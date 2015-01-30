package nntest2.data;

public class BoolData extends Data {	
	public static final BoolData TRUE = new BoolData(true);
	public static final BoolData FALSE = new BoolData(false);
	
	boolean data = false;
	
	public BoolData(boolean data) {
		this.data = data;
	}
	
	public BoolData(String text) throws Exception {
		if(text.compareTo(TRUE.toString()) == 0) {
			data = true;
		} else if(text.compareTo(FALSE.toString()) == 0) {
			data = false;
		} else {
			throw new Exception("bad input");
		}
	}
	
	@Override
	public String toString() {
		return "" + data;
	}
}
