package other;

public interface IWordProcessor {
	void addNextWord(Word word);
	void clearWordContext();	
	void clearCategoryContext();
	boolean finishMultiwordPhrases();
	void updatedCategoryEnd(Word word, String category, Phrase phrase); 
	void updatedCategoryStart(Word word, String category, Phrase phrase);
	boolean isFinished();
}
