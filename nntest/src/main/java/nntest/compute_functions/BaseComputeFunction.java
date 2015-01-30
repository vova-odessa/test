package nntest.compute_functions;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import nntest.BaseNeuron;
import nntest.Util;
import nntest.data.Data;
import nntest.data.DoubleData;
import nntest.interfaces.INeuron;

public abstract class BaseComputeFunction implements IComputeFunction {
	private static final double TETTA_WEIGHT = 0.1;

	private double trainRate = 40.0;
	
	// 0.1-0.5 very low education rate
	// < 10^-7 bad performance
	private double errorMax = 0.001;
	private int maxTrainIterations = 300;
	
	public BaseComputeFunction(double trainRate) {
	}

	public void reCompute(INeuron data) throws Exception {
		data.changeData(new DoubleData(function(data.sumInputs())));
	}

	public void reCompute(INeuron data, int index) throws Exception {
		// index need only when solution depends on current index. Like on softmax layer
		reCompute(data);
	}
	
	public void compute(INeuron data) throws Exception {
		data.addData(new DoubleData(function(data.sumInputs())));
	}

	public void compute(INeuron data, int index) throws Exception {
		// index need only when solution depends on current index. Like on softmax layer
		compute(data);
	}

	public abstract double function(double data);
	public abstract double derivative(double data);

	@Override
	public void train(INeuron data, double correctedValue) {
		if(! (data instanceof BaseNeuron)) {
			return;
			// other not yet supported
		}
		
		data.lock(true);
		
		double err = errorCriteria(data, correctedValue);
		TreeMap<Integer, double[]> W = ((BaseNeuron) data).getW();
		TreeMap<Integer, Data[]> vals = ((BaseNeuron) data).getData();
		double tetta = data.getTetta();
		double out = data.getLastOutput().doubleValue();
		double e = correctedValue - out;
		
		// compute Wsum for all results
		double wSum = Util.sum(W) + TETTA_WEIGHT ; // 1 for tetta
		
		ArrayList<INeuron> neuronsForTrain = new ArrayList<>();
		for(INeuron input: data.getInputNeurons()) {
			if(input.supportTraining()) {
				neuronsForTrain.add(input);
				input.lock(true);
			}
		}
		
		// train kids
		for(INeuron input: neuronsForTrain) {
			double[] w = W.get(input.getId());
						
			double errorWeight = Util.sum(w) / wSum;
			double ei = errorWeight * e;
			
			//data.trainKid(input, new DoubleData(/*input.getLastOutput().doubleValue() + ei*/correctedValue));
			if(correctedValue < errorMax) {
				data.trainKid(input, new DoubleData(-1));
			} else {
				data.trainKid(input, new DoubleData(1));
			}
			
		}
		// train tetta
		data.setTetta(tetta + e*TETTA_WEIGHT/wSum); 
		tetta = data.getTetta();
		
		for(INeuron input: neuronsForTrain) {
			input.lock(false);
		}
		
		// recompute value according to kid changes
		try {
			if(neuronsForTrain.size() > 0) {
				reCompute(data);
			}
		} catch (Exception e2) {
		}
		
		int i = 0;
		
		while((err = errorCriteria(data, correctedValue)) > errorMax) {
			i ++;
			if(i > maxTrainIterations) {
				break;
			}
			
			///// update weights			
			try {			
				double val = data.getLastOutput().doubleValue();
				double error = correctedValue - val; 
				double delta = error * derivative(data.getLastOutput().doubleValue());
				
				for( Entry<Integer, double[]> wSet: W.entrySet() ) {
					double[] w = wSet.getValue();
					Data[] x = vals.get(wSet.getKey());
					
					for(int ind = 0, len = w.length; ind < len; ++ ind) {
						if(x[ind].doubleValue() > 0) {
							double dw = trainRate*delta*x[ind].doubleValue();
							w[ind] = w[ind] + dw;							
						}						
						//w[ind] = w[ind] + trainRate*delta;
					}
				}

				// recompute data according to weight changes
				reCompute(data);
			} catch (Exception e1) {
			}
		}
		// TODO: change tetta again for error
		// if was not able to achieve actual result, update it using tetta.
		out = data.getLastOutput().doubleValue();
		e = correctedValue - out;
		if(Math.abs(e) > 0) {
			data.setTetta(data.getTetta() + e/TETTA_WEIGHT);
			data.changeData(new DoubleData(correctedValue));
		}
		
		data.lock(false);
	}
	
	public double errorCriteria(INeuron data, double expected) {
		double linereErr = error(data, expected);
		
		return 0.5*linereErr*linereErr;
	}
	
	public static double errorCriteria(double data, double expected) {
		double linereErr = data - expected;
		
		return 0.5*linereErr*linereErr;
	}

	private double error(INeuron data, double expected) {
		double sum = data.sumInputs();
		return expected - function(sum);
	}
}
