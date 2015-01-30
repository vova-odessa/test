package nntest.data;

public class DoubleData extends Data<Double>{
	
	public DoubleData(double value) {
		super(value);
	}
	
	@Override
	public int intValue() {
		return value.intValue();
	}

	@Override
	public long longValue() {
		return value.longValue();
	}

	@Override
	public float floatValue() {		
		return value.floatValue();
	}

	@Override
	public double doubleValue() {
		return value.doubleValue();
	}

}
