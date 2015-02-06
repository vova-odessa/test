package nntest2.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public abstract class Data implements Comparable<Data> {	
	public Data[] elements() {
		return new Data[]{this};		
	}
	
	public Data[] forceElements() {
		return elements();		
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
		String string = toString();
		
		if(string != null) {
			return string.hashCode();
		} 
		
		return "null".hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return compareTo((Data) obj) == 0;
	}
	
	public static Data construct(Data[] rawElements) {
		return construct(Arrays.asList(rawElements));		
	}
	
	public static Data construct(Collection<Data> rawElements) {
		Data result = construct(new ArrayData(rawElements).toString(), true);
		
		if(result instanceof StringData) {
			if(rawElements.size() > 1) {
				ArrayList<Data> array = new ArrayList<>(rawElements);
				if(array.get(0).toString().compareTo("(") == 0 && array.get(array.size() - 1).toString().compareTo(")") == 0) {
					return construct(array.subList(1, array.size() - 1));
				} else {
					return new ArrayData(rawElements);
				}
			} else if(rawElements.size() == 1) {
				return rawElements.iterator().next();
			}
		}
		
		return result;
	}
	
	public static Data construct(String data) {
		return construct(data, false);
	}
	
	public static Data construct(String data, boolean singleElement) {
		Data currentData = null;
		
		// try neuron data
		currentData = new NeuronData(data);
		
		if(! ((NeuronData)currentData).isValid() ) {
			try {
				currentData = new BoolData(data);
			} catch (Exception e) {
				currentData = null;
			}
		}
		
		// try index data
		if(currentData == null) {
			try {
				currentData = new VariableData(data);
			} catch(Exception ex) {
			}
		}
		
		// try index data
		if(currentData == null) {
			try {
				currentData = new IndexData(data);
			} catch(Exception ex) {
			}
		}
		
		if(currentData == null) {
			try {
				currentData = new SetData(data);
			} catch(Exception ex) {
				
			}
		}
		
		
		if(currentData == null) {
			if(!singleElement) {
				Data[] elements = ArrayData.splitToElements(data);
				
				if(elements != null) {
					if(elements.length > 1) {
						currentData = new ArrayData(elements);
					} else if(elements.length == 1) {
						currentData = elements[0];
					}
				} else {
					return null;
				}
			} else {
				return new StringData(data);
			}
					
			// TODO later extend there to cover more types
		}
		return currentData;	
	}
	
	public NeuronData toNeuronData() {
		if(this instanceof NeuronData) {
			return (NeuronData) this;
		}
		
		return new NeuronData(toString());
	}
	
	public Data[] splitBy(Data data) {
		ArrayList<Data> splitParts = new ArrayList<>();
		Data[] elements = elements();
		
		ArrayList<Data> currentPart = new ArrayList<>();
		
		for (Data el : elements) {
			if(el.compareTo(data) == 0 || Data.construct(el.toString()).compareTo(data) == 0) {
				if(currentPart.size() > 0) {
					if(currentPart.size() > 1) {
						splitParts.add(new ArrayData(currentPart) );
					} else {
						splitParts.add(currentPart.get(0));
					}
					
					currentPart = new ArrayList<>();
				}
			} else {
				currentPart.add(el);
			}
		}
		
		if(currentPart.size() > 0) {
			if(currentPart.size() > 1) {
				splitParts.add(new ArrayData(currentPart) );
			} else {
				splitParts.add(currentPart.get(0));
			}
		}
		
		if(splitParts.size() > 1) {
			return new ArrayData(splitParts).elements();
		}
		
		if(splitParts.size() == 0) {
			return new Data[0];
		}
		
		return new Data[] {splitParts.get(0)};
	} 
}
