package nntest2.data;

public class TextData extends Data {
	StringData[] parts = null; 
	
	@Override
	public Data[] elements() {
		if(parts != null && parts.length == 1) {
			TextData data = new TextData(parts[0].toString());
			if(data.parts.length == 1) {
				return parts[0].elements();
			}
			
			return data.elements();
		}
		return parts;
	}
	
	@Override
	public String toString() {
		return super.toString(" ");
	}
	
	public TextData(String[] parts) {
		construct(parts);
	}
	
	public TextData(StringData[] parts) {
		construct(parts);
	}
	
	public TextData(String text) {
		construct(translateElements(ArrayData.splitToElements(text)));
	}
	
	public TextData(StringData text) {
		construct(translateElements(ArrayData.splitToElements(text.toString())));
	}
	
	private void construct(StringData[] parts) {
		this.parts = parts;
	}
	
	public static StringData[] translateElements(Data[] elements) {
		if(elements == null) {
			return null;
		}
		
		StringData[] result = new StringData[elements.length];
		
		for(int i = 0; i < elements.length; ++ i) {
			result[i] = elements[i].toStringData();
		}
		
		return result;
	}
	
	private void construct(String[] parts) {
		if(parts != null) {
			StringData[] newParts = new StringData[parts.length];
			
			for (int i = 0, len = parts.length; i < len; ++ i) {
				newParts[i] = new StringData(parts[i].trim());
			}
			construct(newParts);
		} 
	}
}
