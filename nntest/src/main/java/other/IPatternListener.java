package other;

public interface IPatternListener {
	void pastPattern(IWordProcessor processor, Phrase phrase, String category);
	void currentPattern(IWordProcessor processor, Phrase phrase, String category);
	void activated(IWordProcessor processor);
	void rejected(IWordProcessor processor);
	void wordPassed(IWordProcessor processor);
}
