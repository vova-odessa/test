package nntest.compute_functions;

public class HypTanFunction extends BaseComputeFunction {

	double changeRate = 1;
	
	public HypTanFunction(double trainRate, double changeRate) {
		super(trainRate);
		this.changeRate = changeRate;
	}

	@Override
	public double function(double data) {
		double eas = Math.exp(data/changeRate);
		double easRev = 1/eas;
		
		return (eas - easRev)/(eas + easRev);
	}

	@Override
	public double derivative(double data) {
		double val = function(data);
		
		return (1 + val)/(1 - val);
	}

}
