package nntest2.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ArrayData extends Data {
	protected Data[] elements = new Data[]{};
	
	@Override
	public Data[] elements() {
		return elements;
	}
	
	public void setElements(Data[] elements) {
		if(elements != null) {
			this.elements = elements;
		} else {
			this.elements = new Data[]{};
		}
	}	
	
	public ArrayData(Data[] elements) {
		setElements(elements);
	}
	
	public ArrayData(ArrayList<Data> elements) {
		Data[] arr = new Data[elements.size()];
		setElements(elements.toArray(arr));
	}
	
	public void addDelimeter(Data delimeter) {
		int len = Math.max(0, elements.length*2 - 1);
		Data[] newElements = new Data[len];
		
		for(int i = 0, elSize = elements.length; i < elSize; ++ i) {
			newElements[2*i] = elements[i];
			if(2*i + 1 < len) {
				newElements[2*i + 1] = delimeter;
			}
		}
		
		setElements(newElements);
	}
	
	public static Data[] splitToElements(String text) {
		// basic split
		String []parts = text.split("[ \\t\\n\\r]");
		
		ArrayList<Data> result = new ArrayList<>();
		StringBuilder currentPartBuilder = new StringBuilder();
		boolean isInRule = false;
		
		for (String part : parts) {
			currentPartBuilder.append(part);
			
			if( isInRule ) {
				if( part.endsWith("\\)" )) {
					isInRule = false;
				}
			} else if(part.startsWith("(")) {
				isInRule = true;
			}
			
			if(! isInRule) {
				Data currentData = null;
				
				String curText = currentPartBuilder.toString();
				currentData = new NeuronData(curText);
				
				if(! ((NeuronData)currentData).isValid() ) {
					try {
						currentData = new BoolData(curText);
					} catch (Exception e) {
						currentData = null;
					}
				}
				
				if(currentData == null) {
					currentData = new StringData(curText);
					// TODO later extend there to cover more types
				}
				
				result.add(currentData);
				currentPartBuilder = new StringBuilder();
			}
		}
				
		Data [] resultArr = new Data[result.size()];
		return result.toArray(resultArr);
	}
	
	public static ArrayList<Data> subdata(List<Data> list, int from, int to) {
		if(list == null) {
			return new ArrayList<>();
		}
		if(from < 0) {
			from = 0;
		}
		
		if(to > list.size()) {
			to = list.size();
		}
		
		if(from > to) {
			from = to;
		}
		
		return new ArrayList<>(list.subList(from, to));
	}
}
