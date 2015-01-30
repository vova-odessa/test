package nntest.data;

public abstract class Data<DataType> extends Number implements Comparable<Data<DataType>> {
	protected DataType value;
	
	public Data(DataType value) {
		set(value);
	}
	
	public void set(DataType value) {
		this.value = value;
	}

	public int compareTo(Data o) {
		int val = value.getClass().toGenericString().compareTo(o.value.getClass().toGenericString());
		
		if(val != 0) {
			return val;
		}
		
		return new Double(this.doubleValue()).compareTo(new Double(o.doubleValue()));
	}
	
	public Object getData() {
		return value;
	}
	
	public boolean isSameType(Class valueClass) {
		return valueClass.isInstance(value);
	}
}
