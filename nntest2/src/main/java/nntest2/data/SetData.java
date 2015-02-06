package nntest2.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class SetData extends Data {
	HashSet<Data> objects = new HashSet<>();
	
	public SetData(HashSet<Data> objects) {
		this.objects = objects;
	}
	
	@Override
	public Data[] elements() {
		Data[]array = new Data[objects.size()];
		return objects.toArray(array);
	}
	
	public SetData(String data) throws Exception {
		if(data == null || !data.startsWith("{") || !data.endsWith("}")) {
			throw new Exception();
		}
		
		//String elements = data.substring(beginIndex)
		String content = data.substring(1, data.length() - 1).trim();
		
		while(content.length() > 0) {
			int posComa = content.indexOf(",");
			int posBr = content.indexOf("{");
			int posBr2 = content.indexOf("}");
			
			String subContent ;
			
			if(posBr == 0 && posBr2 != -1) {
				subContent = content.substring(posBr + 1, posBr2).trim();
				content = content.substring(posBr2 + 1).trim();				
			} else if(posComa != -1) {
				subContent = content.substring(0, posComa).trim();
				content = content.substring(posComa + 1).trim();
			} else {
				subContent = content;
				content = "";
			}
			
			objects.add(Data.construct(subContent));
		}
	} 
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("{");
		
		for (Data obj : objects) {
			if(result.length() > 1) {
				result.append(", ");
			}
			result.append(obj);
		}
		
		result.append("}");
		
		return result.toString();
	}
}
