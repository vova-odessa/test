package nntest2.data;

public class LetterData extends Data {
	private char letter;
	
	public LetterData(char letter) {
		this.letter = letter;
	}
	
	@Override
	public String toString() {
		return new String(new char[]{letter});
	}
}
