package nntest.identifiers;

import java.util.Map.Entry;

import nntest.data.Data;
import nntest.data.DoubleData;
import nntest.data.NeuronData;
import nntest.interfaces.INeuron;

public class MaxInputSelector extends ANeuronIdentifier {
	
	public MaxInputSelector() {		
	}

	public MaxInputSelector(int memorySize) {
		super(memorySize);
	}

	@Override
	public void compute() throws Exception {
		Data max = null;
		Data maxValue = null;
		for(Entry<INeuron, Data> input : inputData.entrySet()) {
			if( max == null || input.getValue().compareTo(maxValue) > 0 ) {
				max = new NeuronData(input.getKey());
				maxValue = input.getValue();
			}
		}
		
		if(maxValue instanceof DoubleData) {
			if(maxValue.doubleValue() == 0) {
				addData(new NeuronData(null));
				return;
			}
		}
		
		addData(max);
	}

	@Override
	public Data train(Data expected) {
		INeuron expN = null;
		int id = 0;
		
		if(expected instanceof NeuronData) {
			expN = (INeuron) expected.getData();
		} else {
			id = expected.intValue();
		}
		
		for (Entry<INeuron, Data> inputDataE : inputData.entrySet()) {
			Data expectation = null;
			if(expN != null && expN.compareTo(inputDataE.getKey()) == 0
				|| expN == null && inputDataE.getKey().getId() == id) {
				expectation = new DoubleData(1);
			} else {
				expectation = new DoubleData(0);
			}
			trainKid(inputDataE.getKey(), expectation);
		}
		
		return getLastOutput();
	}

	@Override
	public void trainKid(INeuron kid, Data expected) {
		kid.train(expected);
	}

	@Override
	public INeuron create() {
		return new MaxInputSelector(state.size());
	}

}
