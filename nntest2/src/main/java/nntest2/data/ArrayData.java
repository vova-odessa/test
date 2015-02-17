package nntest2.data;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.sec.gb.ipa.ks.common.data.Pair;


public class ArrayData extends Data {
	private static HashMap<String, String> brackets = new HashMap<>();
	
	static {
		brackets.put("(", ")");
		brackets.put("[", "]");
		brackets.put("{", "}");
	}
	
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
	
	public ArrayData(Collection<Data> elements) {
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
		
		for (Entry<String, String> bracket : brackets.entrySet()) {
			try {
				text = text.replaceAll(bracket.getKey(), " " + bracket.getKey() + " ");
				text = text.replaceAll(bracket.getValue(), " " + bracket.getValue() + " ");
			} catch(Throwable t) {
				text = text.replaceAll("\\" + bracket.getKey(), " " + bracket.getKey() + " ");
				text = text.replaceAll("\\" + bracket.getValue(), " " + bracket.getValue() + " ");
			}
		}
		
		String []parts = text.split("[ \\t\\n\\r]");
				
		if(parts.length == 1) {
			return new Data[]{ Data.construct(text, true)};
		}
				
		ArrayList<Data> result = new ArrayList<>();
		
		for (int i = 0, num = parts.length; i < num; ++ i) {
			Pair<Data, Integer> locResult = splitElementsStartFrom(parts, i);
			
			if(locResult == null) {
				break;
			}
			
			if(locResult.first != null) {
				if(locResult.first instanceof ArrayData) {
					result.addAll(Arrays.asList(locResult.first.elements()));
				} else {
					result.add(locResult.first);
				}
			}
			
			i = locResult.second;
		}
						
		Data [] resultArr = new Data[result.size()];
		return result.toArray(resultArr);
	}
	
	public static Pair<Data, Integer> splitElementsStartFrom(String[] elements, int start) {
		if(elements == null || elements.length == 0) {
			return null;
		}
		
		String end = brackets.get(elements[start]);
		ArrayList<Data> listData = new ArrayList<>();
		
		int i = start;
		
		if(end != null) {
			++ i;
			listData.add(new StringData(elements[start]));
		}
		
		for(; i < elements.length; ++ i) {
			String word = elements[i];
			
			if(word == null || word.trim().length() == 0) {
				continue;
			}
			
			if(end != null && word.compareTo(end) == 0) {
				listData.add(new StringData(word));
				break;
			} else if(brackets.containsKey(word)) {
				// start of new data
				Pair<Data, Integer> result = splitElementsStartFrom(elements, i);
				if(result == null) {
					break;
				}
				
				if(result.first != null) {
					listData.add(result.first);
				}
				
				i = result.second;
			} else {
				listData.add(Data.construct(word));
			}
		}
				
		if(listData == null || listData.size() == 0) {
			return new Pair<Data, Integer>(null, i); 
		}
				
		return new Pair<Data, Integer>(Data.construct(listData), i);
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
	
	public ArrayList<Data> list() {				
		return new ArrayList<>(Arrays.asList(elements));
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		
		for (Data data : elements) {
			if(result.length() > 0) {
				result.append(" ");
			}
			
			result.append(data);
		}
		
		return result.toString();
	}
}
