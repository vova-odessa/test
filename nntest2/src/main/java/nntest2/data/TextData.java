package nntest2.data;

import java.util.Collection;
import java.util.Collections;

public class TextData extends Data {
	Data[] parts = null; 
	
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
		constructText(parts);
	}
	
	public TextData(Data[] parts) {
		constructText(parts);
	}
	
	public TextData(String text) {
		//construct(translateElements(ArrayData.splitToElements(text)));
		constructText(Data.construct(text).elements());
	}
	
	public TextData(StringData text) {
		constructText(Data.construct(text.toString()).elements());
	}
	
	private void constructText(Data[] parts) {
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
			Data[] newParts = new Data[parts.length];
			
			for (int i = 0, len = parts.length; i < len; ++ i) {
				newParts[i] = //new StringData(parts[i].trim());
						Data.construct(parts[i].trim());
			}
			constructText(newParts);
		} 
	}
	
	public static TextData constructPossibilities(Collection<Data> possibilities) {
		if(possibilities == null || possibilities.size() == 0) {
			return null;
		}
		
		Data[] array = new Data[possibilities.size()];
		// XXX extend in future
		return new TextData(possibilities.toArray(array));
	}
}
