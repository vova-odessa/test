package nntest2.herpers;

import nntest2.data.Data;

public class StringHelper {
	public static boolean isAllSame(Data data) {
		String first = data.toString();
		boolean isAllSame1 = false;
		if(first.length() > 1) {
			isAllSame1 = first.replaceAll("" + first.charAt(0), "").length() == 0;
		}
		
		return isAllSame1;
	}
}
