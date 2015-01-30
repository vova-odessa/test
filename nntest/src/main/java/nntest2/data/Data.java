package nntest2.data;

public abstract class Data implements Comparable<Data> {	
	public Data[] elements() {
		return new Data[]{this};		
	}
	
	@Override
	public int compareTo(Data other) {
		return toString().compareTo(other.toString());
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		for (Data element : elements()) {
			if(element != this) {
				builder.append(element.toString());
			}
		}
		
		return builder.toString();
	}
	
	public String toString(String delimeter) {
		if(delimeter.length() > 0) {
			ArrayData data = new ArrayData(elements());
			data.addDelimeter(new StringData(delimeter));
			return data.toString();
		} else {
			return toString();
		}
	}
	
	public StringData toStringData() {
		return new StringData(toString());
	}
	
	public boolean contains(Data other) {
		return toString().contains(other.toString());
	}
	
	public Data[] split(Data other) {
		TextData data = new TextData(toString().split(other.toString().replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)")));
		ArrayData array = new ArrayData(data.elements());
		array.addDelimeter(other);
		
		return array.elements;
	}
	
	public boolean isAllDifferent(Data other) {
		return toString().matches("[" + other.toString() + "]");
	}
	
	public boolean isTrue() {
		return compareTo(new BoolData(true)) == 0;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return compareTo((Data) obj) == 0;
	}
	
	public static Data construct(String data) {
		return new TextData(data);		
	}
}
