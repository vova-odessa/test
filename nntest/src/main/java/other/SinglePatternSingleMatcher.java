package other;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sec.gb.ipa.ks.common.data.Pair;

public class SinglePatternSingleMatcher implements IWordProcessor {
	private static final String ANY_CATEGORY_NAME = "*";

	private static Logger logger = LoggerFactory.getLogger(SinglePatternSingleMatcher.class);
	
	private HashSet<IPatternListener> listeners = new HashSet<>();
	private Pattern pattern = null;
	private Word lastWord = null;
	private Phrase lastPhrase = new Phrase();
	private String requiredCategory = null;
	private boolean state = true;
	private boolean stateCurrent = true;
	private ArrayList<Phrase> phrases = new ArrayList<>();
	private boolean activated = false;
	private boolean removed = false;
	
	public void listen(IPatternListener listener) {
		listeners.add(listener);
	}
	
	public void unlisten(IPatternListener listener) {
		listeners.remove(listener);
	}
	
	public SinglePatternSingleMatcher(Pattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public void addNextWord(Word word) {
		if(isFinished()) {
			return;
		}
		
		Phrase prev = lastPhrase;
		if(prev == null || prev.words.size() == 0) {
			if(phrases.size() > 0) {
				prev = phrases.get(phrases.size() - 1);
			}
		} 
		
		if(prev.end() != null && prev.end().id >= word.id) {
			return;
		}
		
		lastPhrase.add(word);
		
		if(stateCurrent != true) {
			return;
		}
		
		if(state == true) {
			lastWord = word;
			
			checkWord(word);
			
			//if(stateCurrent == true && isFinalized()) {
			//	finishPhrase();
			//}
		}
	}

	private void checkWord(Word word) {
		if(!state || activated) {
			return;
		}
		
		int index = phrases.size();
		if(pattern.requireWord(index)) {
			if(word.id == -1) {
				// skip
				return;
			}
			if(!pattern.match(word.word, index)) {
				// if it require word, and not match, there no way to restore
				state = false;
				stateCurrent = false;
				dispose();
			} else {				
				stateCurrent = true;
			}
		} else {
			requiredCategory = pattern.requiredCategory(index); 
			
			if(requiredCategory == null) {
				// something is wrong here
				state = false;
				stateCurrent = false;
				dispose();
			}
					
			if(requiredCategory.startsWith("<")) {
				requiredCategory = requiredCategory.substring(1, requiredCategory.length() - 1);
			}
			
			String nextCategory = pattern.requiredCategory(index + 1);
			if(nextCategory == null || !nextCategory.startsWith("<") ) {
				nextCategory = "";
			} else {
				nextCategory = nextCategory.substring(1, nextCategory.length() - 1);
			}
			
			if(word.id == -1) {
				if(requiredCategory.equals(ANY_CATEGORY_NAME)) {
					stateCurrent = true;
					requiredCategory = null;
				} else {
					state = false;
					stateCurrent = false;
					dispose();				
				}
			} else if(requiredCategory.length() == 0 || word.hasCategory(requiredCategory)) {
				stateCurrent = true;
			} else if(requiredCategory.equals(ANY_CATEGORY_NAME) && word.hasCategory(nextCategory)) {
				phrases.add(lastPhrase.substract(0, lastPhrase.words.size() - 1));
				lastPhrase = new Phrase();
				lastPhrase.add(word);
				stateCurrent = true;
				requiredCategory = nextCategory;
			} else {
				stateCurrent = false;
				// TODO check is need below after each new word
				word.listenCategories(this);
			}
		}
		
		if(stateCurrent == true) {
			boolean isChanged = false;
			if(word.id != -1 && requiredCategory != null) {
				Phrase phrase = null;
				if(requiredCategory.length() > 0) { 
					phrase = word.getCategories().get(requiredCategory);
				} else {
					phrase = lastPhrase.substract(0, 1);
				}
				
				if(phrase != null) {
					lastPhrase = lastPhrase.substract(phrase);
					
					if(lastPhrase != null && lastWord != null && (lastPhrase.start() == null || lastWord.id != lastPhrase.start().id)) {
						lastWord = lastPhrase.start();
						isChanged = true;
						phrases.add(phrase);
						requiredCategory = null;
					}					
				}
			} else {
				phrases.add(lastPhrase);
				lastPhrase = new Phrase();
				lastWord = null;
			}
			
			if(isChanged && lastWord != null) {
				checkWord(lastWord);
			}
			
			if(isFinalized()) {
				activate();
			}
		}
	}

	private boolean isFinalized() {
		return stateCurrent == true && pattern.isFinalize(phrases.size() - 1);
	}

	private void finishPhrase() {
		phrases.add(lastPhrase);
		lastPhrase = new Phrase();
	}

	@Override
	public void clearWordContext() {
		dispose();
	}

	@Override
	public void clearCategoryContext() {
		dispose();
	}

	private void dispose() {
		removed = true;
		state = false;
		stateCurrent = false;
		
		for (IPatternListener listener : listeners) {
			listener.rejected(this);
		}
		listeners.clear();
		phrases = null;		
	}

	@Override
	public void updatedCategoryEnd(Word word, String category, Phrase phrase) {
		if(state == true && stateCurrent == false) {
			// XXX Probably can avoid some checks in future by stop waiting after this
		} else {
			if(stateCurrent == true || lastWord != null && !word.equal(lastWord)) {
				word.stopListen(this);
			}
		}
	}

	@Override
	public void updatedCategoryStart(Word word, String category, Phrase phrase) {
		if(!category.startsWith(Pattern.CATEGORY_PREFIX)) {
			category = Pattern.CATEGORY_PREFIX + category + Pattern.CATEGORY_ENDING;
		}
		if(state == true && stateCurrent == false) {
			if(category.startsWith("<")) {
				category = category.substring(1, category.length() - 1);
			}
			
			if(requiredCategory.compareTo(category) == 0) {
				if(lastPhrase.starts(phrase)) {
					lastPhrase = lastPhrase.substract(phrase);
					phrases.add(phrase);
					
					if(lastPhrase == null) {
						logger.error("updatedCateforyStart: failed to substruct ");
						lastPhrase = new Phrase();
					}
					
					word.stopListen(this);
					lastWord = lastPhrase.start();
					
					if(lastWord != null) {
						// case when only part of tail passes, we should continue check kept tail.
						checkWord(lastWord);
					} else {
						stateCurrent = true;
						
						if(isFinalized()) {
							activate();
						}
					}
				}
			}
		} else {
			word.stopListen(this);
		}
	}
	
	private void activate() {
		if(activated) {
			logger.error("activate: already activated");
		}
		
		activated = true;
		ArrayList<Pair<Phrase, String>> result = pattern.getResult(phrases);
		Phrase last = phrases.get(phrases.size() - 1);
		
		for (Pair<Phrase, String> category : result) {
			if(category.first.ends(last)) {
				for (IPatternListener listener : listeners) {
					listener.currentPattern(this, category.first, category.second);
				}
			} else {
				for (IPatternListener listener : listeners) {
					listener.pastPattern(this, category.first, category.second);
				}
			}
		}
		
		for (IPatternListener listener : listeners) {
			listener.activated(this);
		}
	}

	@Override
	public boolean finishMultiwordPhrases() {
		checkWord(new Word("#", -1));
		return isFinished();
	}

	@Override
	public boolean isFinished() {		
		return !state || activated || removed;
	}
	
	@Override
	public String toString() {
		StringBuilder resultBuilder = new StringBuilder();
		resultBuilder.append("{");
		
		if(pattern != null) {
			resultBuilder.append("pattern = ");
			resultBuilder.append(pattern.toString());
			resultBuilder.append(", ");
		}
		
		if(phrases != null) {
			resultBuilder.append("phrases = ");
			resultBuilder.append(phrases.toString());
			resultBuilder.append(", ");
		}
		
		resultBuilder.append("}");
		return super.toString();
	}
	
}
