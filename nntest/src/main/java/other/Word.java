package other;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.sec.gb.ipa.ks.common.data.Pair;

public class Word implements Comparable<Word> {
	public String word;
	private HashMap<String, Phrase> categories = new HashMap<>();
	private Set<IWordProcessor> listeners = new HashSet();
	public long id;
	
	public Word(String word, int id) {
		this.word = word;
		this.id = id;
	}
	
	public void listenCategories(IWordProcessor listener) {
		if(listener != null) {
			listeners.add(listener);
		}
	}
	
	public void stopListen(IWordProcessor listener) {
		listeners.remove(listener);
	}

	@Override
	public int compareTo(Word other) {
		return word.compareTo(other.word);
	}
	
	
	public boolean same(Word other) {
		return id == other.id;
	}
	
	public boolean equal(Word other) {
		return compareTo(other) == 0;
	}
	
	public boolean addCategory(Phrase phrase, String category) {
		if(hasCategory(category)) {
			return false;
		}
		categories.put(category, phrase);
		ArrayList<IWordProcessor> listenersCopy = new ArrayList<>(listeners);
		
		for (IWordProcessor listener : listenersCopy) {			
			listener.updatedCategoryEnd(phrase.end(), category, phrase);
			listener.updatedCategoryStart(phrase.start(), category, phrase);			
		}
		
		return true;
	}
	
	public HashMap<String, Phrase> getCategories() {
		return categories;
	}
	
	public boolean hasCategory(String category) {
		return categories.containsKey(category);
	}
}
