package nntest2.data;

public class RealData extends Data {
	double data = 0;
	
	public double getData() {
		return data;
	}
	
	public RealData( double data ) {
		this.data = data;
	}
	
	public RealData( String data ) throws Exception {
		try {
			this.data = Double.parseDouble(data);
		} catch(Exception e) {
			throw e;
		}
	}
	
	@Override
	public String toString() {
		return "" + data;
	}
}
