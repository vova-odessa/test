package other;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sec.gb.ipa.ks.common.data.Pair;

/**
 *	use <category> for category matching
 *  use [x] for variable specification
 *  use [x] [x] ... [x] for variable those contains many words  
 * 
 * @author sec
 *
 */
public class Pattern implements Comparable<Pattern> {
	static Logger logger = LoggerFactory.getLogger(Pattern.class);
	
	public static final String CATEGORY_PREFIX = "<";
	public static final String CATEGORY_ENDING = ">";
	
	// the initial words or whole category in < > 
	public ArrayList<String> words = new ArrayList<>();
	
	// group indexes for previous
	public ArrayList<Integer> groups = new ArrayList<>();
	
	// the out categories
	public ArrayList<String> categories = new ArrayList<>();
	public String wholeCategory = null;
	
	public void addPhrase(String phrase, String category) {
		String[] words = phrase.split("[ \t,:]");
		
		StringBuilder wordBuilder = new StringBuilder();
		int group = 1;
		if(groups.size() > 0) {
			group = groups.get(groups.size() - 1) + 1;
		}
		
		for (String word : words) {
			
			wordBuilder.append(word);
			
			if(wordBuilder.toString().startsWith(CATEGORY_PREFIX) && !word.endsWith(CATEGORY_ENDING)) {
				continue;
			}
 			
			this.words.add(wordBuilder.toString());
			groups.add(group);
			categories.add(category);
			
			wordBuilder = new StringBuilder();
		}
		
		if(wordBuilder.length() > 0) {
			logger.error("Unfinalized category : " + wordBuilder.toString());
		}
	}
	
	public void assignCategory(String cat) {
		wholeCategory = cat;
	}
	
	public boolean assign(String[] phrases, String []categories) {
		if(phrases == null || categories == null || phrases.length != categories.length) {
			return false;
		}
		
		for(int i = 0; i < phrases.length; ++ i) {
			addPhrase(phrases[i], categories[i]);
		}
		
		return true;
	}
	
	public boolean match(String word, int index) {
		if(index >= words.size()) {
			return false;
		}
		
		return words.get(index).compareTo(word) == 0;
	}
	
	public boolean isFinalize(int index) {
		return index == words.size() - 1;
	}
	
	/**
	 * Returns array list of producted phrase->category
	 * @return
	 */
	public ArrayList<Pair<Phrase, String>> getResult(Phrase[] phrases) {
		if(words.size() != phrases.length) {
			logger.error("getResult: pattern and request are not match by size");
			return null;
		}
		
		ArrayList<Pair<Phrase, String>> result = new ArrayList<>();
		
		int lastGroup = groups.get(0);
		Phrase phraseBuilder = new Phrase();
		Phrase allBuilder = new Phrase();
		
		for(int i = 0; i < phrases.length; ++ i) {
			if(wholeCategory != null) {
				if(!allBuilder.add(phrases[i])) {
					logger.error("Can't add phrase");
				}
			}
			
			if(groups.get(i) != lastGroup) {
				String category = categories.get(lastGroup - 1);
				if(category != null && category.length() > 0) {
					result.add(new Pair<Phrase, String>(phraseBuilder, category));				
				}
				lastGroup = groups.get(i);
				phraseBuilder = new Phrase();
			}
			
			phraseBuilder.add(phrases[i]);
		}
		
		if(phraseBuilder.words.size() > 0) {
			String category = categories.get(lastGroup - 1);
			if(category != null && category.length() > 0) {
				result.add(new Pair<Phrase, String>(phraseBuilder, categories.get(lastGroup - 1)));
			}
		}
		
		if(wholeCategory != null && !(categories.size() == 1 && wholeCategory.compareTo(categories.get(0)) == 0 && allBuilder.words.size() == phraseBuilder.words.size())) {
			result.add(new Pair<Phrase, String>(allBuilder, wholeCategory));
		}
		
		return result;
	}
	
	public ArrayList<Pair<Phrase, String>> getResult(List<Phrase> phrases) {
		Phrase arr[] = new Phrase[phrases.size()]; 
		return getResult(phrases.toArray(arr));
	}
	
	public boolean requireWord(int index) {
		if(index >= words.size()) {
			return true;
		}
		
		return !words.get(index).startsWith(CATEGORY_PREFIX);
	}
	
	public String requiredCategory(int index) {
		if(requireWord(index)) {
			return null;
		}
		
		return words.get(index);
	}
	
	public String start() {
		if(words.size() == 0) {
			return null;
		} else {
			return words.get(0);
		}
	}

	@Override
	public int compareTo(Pattern other) {
		if(words.size() != other.words.size()) {
			return (words.size() > other.words.size()?1:-1);
		}
		
		for(int i = 0; i < words.size(); ++ i) {
			int res = words.get(i).compareTo(other.words.get(i));
			
			if(res != 0) {
				return res;
			}
		}
		
		if(wholeCategory == null) {
			if(other.wholeCategory == null) {
				return 0;
			} else {
				return -1;
			}
		}
		
		return wholeCategory.compareTo(other.wholeCategory);
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Pattern)) {
			return false;
		}
		return compareTo((Pattern) other) == 0;
	}
	
	@Override
	public int hashCode() {
		return words.hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder resBuilder = new StringBuilder();
		resBuilder.append("{");
		
		for (String word : words) {
			resBuilder.append(word);
			resBuilder.append(" ");
		}
		
		resBuilder.append("}:");
		
		if(wholeCategory != null) {
			resBuilder.append(wholeCategory);
		}
		
		return resBuilder.toString();
	}
}
