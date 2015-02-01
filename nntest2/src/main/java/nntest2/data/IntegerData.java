package nntest2.data;

public class IntegerData extends Data{
	private long data;
	
	public IntegerData(int data) {
		this.data = data;
	}
	
	public IntegerData(long data) {
		this.data = data;
	}
	
	@Override
	public String toString() {
		return "" + data;
	}
}
