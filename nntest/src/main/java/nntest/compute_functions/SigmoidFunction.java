package nntest.compute_functions;

import nntest.BaseNeuron;

public class SigmoidFunction extends BaseComputeFunction{

	double changeRate = 1;
	
	public SigmoidFunction(double trainRate, double changeRate) {
		super(trainRate);
		this.changeRate = changeRate;
	}

	@Override
	public double function(double data) {
		return 1 / (1 + Math.exp(- changeRate*data));
	}

	@Override
	public double derivative(double data) {
		double val = function(data);
		
		return val*(1-val);
	}



}
