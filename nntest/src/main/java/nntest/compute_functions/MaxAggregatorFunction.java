package nntest.compute_functions;

import java.security.AllPermission;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import nntest.BaseNeuron;
import nntest.data.Data;
import nntest.data.DoubleData;
import nntest.interfaces.INeuron;

public class MaxAggregatorFunction implements IComputeFunction {	
	private static final int MAX_TOP_CLASS = 5;
	Logger logger = Logger.getLogger(MaxAggregatorFunction.class);
	public enum NormalzeStrategy{AS_IS, EQ_1, ALL_NORMALIZE, SOFT_MAX, DOMINATING_SOFT_MAX};
	NormalzeStrategy strategy = NormalzeStrategy.AS_IS;
	double dominateDifference = 0.005;
	
	MaxAggregatorFunction() {
		
	}
	
	public MaxAggregatorFunction(NormalzeStrategy strategy) {
		this.strategy = strategy;
	}
	
	public void compute(INeuron data) throws Exception {
		if(! (data instanceof BaseNeuron)) {
			return;
			// other not yet supported
		}
		TreeMap<Integer, Data[]> vals = ((BaseNeuron) data).getData();
		TreeMap<Integer, double[]> W = ((BaseNeuron) data).getW();
		
		if(vals.size() != W.size()) {
			throw new Exception("vals size not equal to w size");
		}
				
		double max = -99999;
		double sum = 0.0;
		int maxInd = -1;
		
		double valsAsMax = 1;
		
		for(Entry<Integer, Data[]> entry: vals.entrySet()) {
			/*double val = 0.0;// entry.getValue()[0].doubleValue();
			double w = 0.0; // W.get(entry.getKey())[0];
			
			if(entry.getValue()[0].isSameType(BaseNeuron.class)) {
				
			} else {
				if(W.containsKey(entry.getKey())) {
					
				} else {
					logger.error("Wrong value - weight pair. Key = " + entry.getKey());
					continue;
				}
			}*/
			
			double resultVal = //val * w;
					((BaseNeuron) data).computeInput(entry.getKey(), entry.getValue(), false);
			
			if(Math.abs(max - resultVal) < dominateDifference) {
				++ valsAsMax;
			} else if(resultVal > max) {
				valsAsMax = 1;
			}
			
			if(resultVal > max) {
				max = resultVal;
				maxInd = entry.getKey();
			}
			
			switch (strategy) {
			case ALL_NORMALIZE:
				sum += resultVal;
				break;
			case SOFT_MAX:
			case DOMINATING_SOFT_MAX:
				sum += Math.exp(resultVal);
				break;
			}
		}
		
		if(valsAsMax > MAX_TOP_CLASS) {
			data.addData(new DoubleData(0.0));
			return;
		}
		
		switch (strategy) {
		case SOFT_MAX:
		case DOMINATING_SOFT_MAX:
			max = Math.exp(max);
		case ALL_NORMALIZE:
			max = max/sum;
			break;
		case EQ_1:
			max = 1.0/valsAsMax;
			break;		
		}
		
		data.addData(new DoubleData(max));
	}

	public void compute(INeuron data, int index) throws Exception {
		if(! (data instanceof BaseNeuron)) {
			return;
			// other not yet supported
		}
		
		TreeMap<Integer, Data[]> vals = ((BaseNeuron) data).getData();
		TreeMap<Integer, double[]> W = ((BaseNeuron) data).getW();
		
		if(vals.size() != W.size()) {
			throw new Exception("vals size not equal to w size");
		}
		
		if(!vals.containsKey(index)) {
			throw new Exception("input not have requested index");
		}
				
		double max = -99999;
		double sum = 0.0;
		int maxInd = -1;
		double valsAsMax = 0;
		
		for(Entry<Integer, Data[]> entry: vals.entrySet()) {
			/*double val = entry.getValue()[0];
			double w = W.get(entry.getKey())[0];
			
			double resultVal = val * w;*/
			
			double resultVal = //val * w;
					((BaseNeuron) data).computeInput(entry.getKey(), entry.getValue(), false);
			
			if(Math.abs(max - resultVal) <= dominateDifference) {
				++ valsAsMax;
			} else if(resultVal > max) {
				valsAsMax = 1;
			}
			
			if(resultVal > max) {
				max = resultVal;
				maxInd = entry.getKey();
			}
			
			switch (strategy) {
			case ALL_NORMALIZE:
				sum += resultVal;
				break;
			case SOFT_MAX:
			case DOMINATING_SOFT_MAX:
				sum += Math.exp(resultVal);
				break;
			}
		}
		
		//double val = vals.get(index)[0] * W.get(index)[0];
		double val = ((BaseNeuron) data).computeInput(index, vals.get(index), false);
		
		if(valsAsMax > MAX_TOP_CLASS || max < dominateDifference) {
			data.addData(new DoubleData(0.0));
			return;
		}
		
		switch (strategy) {			
		case SOFT_MAX:
		case DOMINATING_SOFT_MAX:
			val = Math.exp(val);
			max = Math.exp(max);
		case ALL_NORMALIZE:
			if( maxInd != index && strategy == NormalzeStrategy.DOMINATING_SOFT_MAX && Math.abs(val - max) > dominateDifference) {
				val = 0;
			} else {
				val = val / sum;
			}
			break;
		case EQ_1:
			if( Math.abs(val - max) > dominateDifference ) {
				val = 0;
			} else {
				val = 1.0/valsAsMax;
			}
			break;		
		case AS_IS:
			if( maxInd != index ) {
				val = 0;
			}
			break;
		}
		
		data.addData(new DoubleData(val));
	}

	public void train(INeuron data, double correctedValue) {
		for(INeuron input : data.getInputNeurons()) {
			if(input.getId() == data.getId()) {
				data.trainKid(input, new DoubleData(correctedValue));
			}
		}
	}


}
