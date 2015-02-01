package nntest2.data;

public class StringData extends Data {
	private String data = new String();
	
	public StringData(String data) {
		this.data = data;
	}

	@Override
	public Data[] elements() {
		Data[] dataArr = new Data[data.length()];
		for(int i = 0, len = data.length(); i < len; ++ i) {
			dataArr[i] = new LetterData(data.charAt(i));
		}
		
		return dataArr;
	}
		
	@Override
	public String toString() {
		return data;
	}
}
