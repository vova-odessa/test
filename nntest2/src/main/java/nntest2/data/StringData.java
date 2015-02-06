package nntest2.data;

public class StringData extends Data {
	private String data = new String();
	
	public StringData(String data) {
		this.data = data;
	}
	
	public StringData(Data[] data) {
		StringBuilder builder = new StringBuilder();
		for (Data data2 : data) {
			builder.append(data2);
		}
		
		this.data = builder.toString();
	}

	@Override
	public Data[] elements() {
		/*Data[] dataArr = new Data[data.length()];
		for(int i = 0, len = data.length(); i < len; ++ i) {
			dataArr[i] = new LetterData(data.charAt(i));
		}
		
		return dataArr;*/
		return new Data[]{this};
	}
		
	@Override
	public String toString() {
		return data;
	}
	
	@Override
	public Data[] forceElements() {
		Data[] dataArr = new Data[data.length()];
		for(int i = 0, len = data.length(); i < len; ++ i) {
			dataArr[i] = new LetterData(data.charAt(i));
		}
		
		return dataArr;
	}
}
