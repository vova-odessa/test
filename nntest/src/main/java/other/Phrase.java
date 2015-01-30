package other;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Phrase {
	private static Logger logger = LoggerFactory.getLogger(Phrase.class);
	public ArrayList<Word> words = new ArrayList<>();
	private String text = null;
	
	public boolean add(Word word) {
		if(isAddable(word)) {		
			words.add(word);
			text = null;
			return true;
		}
		return false;		
	}

	public boolean isAddable(Word word) {
		Word lastWord = null;
		if(words.size() > 0) {
			lastWord = words.get(words.size() - 1);
		}
		
		if(lastWord != null && lastWord.id + 1 != word.id) {
			return false;
		}
		
		return true;
	}
	
	public boolean ends(Word word) {
		if(words.size() == 0) {
			return false;
		}
		
		return words.get(words.size() - 1).same(word);
	}
	
	public boolean ends(Phrase phrase) {
		if(phrase.words.size() == 0 || words.size() < phrase.words.size()) {
			return false;
		}
		
		return ends(phrase.words.get(phrase.words.size() - 1));
	}
	
	public boolean starts(Word word) {
		if(words.size() == 0) {
			return false;
		}
		
		return words.get(0).same(word);
	}
	
	public boolean starts(Phrase phrase) {
		if(phrase.words.size() == 0) {
			return false;
		}
		
		return starts(phrase.words.get(0));
	}
	
	public Word start () {
		if( words.size() == 0 ) {
			return null;
		}
		
		return words.get(0);
	}
	
	public Phrase substract(Phrase phrase) {
		if(!starts(phrase)) {
			return null;
		}
		
		Phrase result = new Phrase();
		for(int i = phrase.words.size(); i < words.size(); ++ i) {
			if(! result.add(words.get(i))) {
				logger.error("substract: Failed to add words from initial phrase");
				return result;
			}
		}
		
		return result;
	}
	
	public Phrase substract(Word word) {
		if(!starts(word)) {
			return null;
		}
		
		Phrase result = new Phrase();
		for(int i = 1; i < words.size(); ++ i) {
			if(! result.add(words.get(i))) {
				logger.error("substract: Failed to add words from initial phrase");
				return result;
			}
		}
		
		return result;
	}
	
	public Phrase substract(int from, int to) {
		Phrase result = new Phrase();
		
		if(from >= to || from < 0 || to > words.size()) {
			return result;
		}
		
		for(int i = from; i < to; ++ i) {
			if(! result.add(words.get(i))) {
				logger.error("substract: Failed to add words from initial phrase");
				return result;
			}
		}
		
		return result;
	}
	
	public boolean add(Phrase phrase) {
		if(phrase.words.size() == 0) {
			return true;
		}
		
		if(!isAddable(phrase.words.get(0))) {
			return false;
		}
		
		text = null;
		words.addAll(phrase.words);
		
		return true;
	}
	
	public Word end() {
		if(words.size() > 0) {
			return words.get(words.size() - 1);
		}
		
		return null;
	}
		
	@Override
	public String toString() {		
		if(text == null) {
			StringBuilder builder = new StringBuilder();
			for (Word word : words) {
				if(builder.length() > 0) {
					builder.append(" ");
				}
				builder.append(word.word);
			}
			
			return text = builder.toString();
		}
		
		return text;
	}
}
