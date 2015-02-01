package nntest2.herpers;

import java.util.ArrayList;

public class CommonHelper {
	public static ArrayList mergeCopy(ArrayList orig, Object data) {
		ArrayList copy = new ArrayList<>();
		copy.addAll(orig);
		copy.add(data);
		return copy;
	}
	
	public static ArrayList mergeCopy(Object data) {
		ArrayList copy = new ArrayList<>();
		copy.add(data);
		return copy;
	}
	
	public static ArrayList mergeCopy(Object orig, Object data) {
		ArrayList copy = new ArrayList<>();
		copy.add(orig);
		copy.add(data);
		return copy;
	}
}
