package nntest.identifiers;

import java.util.HashMap;

import org.apache.log4j.Logger;

import nntest.data.Data;
import nntest.data.DoubleData;
import nntest.data.FuzzyValue;
import nntest.interfaces.INeuron;

public class MultyClassInputSignal extends ANeuronIdentifier {
	Logger logger = Logger.getLogger(MultyClassInputSignal.class);
	
	public MultyClassInputSignal(int memorySize) {
		super(memorySize);
	}
	
	HashMap<Data, FuzzyValue> experience = new HashMap<>();

	/**
	 * Assumes, there only 1 input, that can encode many classes inside.
	 * Compute using 2 param fuzzy logic.
	 * - signal - 0, this class lead to 0 signal
	 * - experience - number of train data assumed
	 * 
	 */
	@Override
	public void compute() throws Exception {
		if(inputData.size() != 1) {
			logger.error("MultyClassInputSignal::compute - wrong input");
			return;
		}
		
		Data input = inputData.values().iterator().next();
		
		if(!experience.containsKey(input)) {
			experience.put(input, new FuzzyValue());
		}
		
		addData(new DoubleData(experience.get(input).getSignal()));		
	}

	@Override
	public Data train(Data expected) {
		Data input = inputData.values().iterator().next();
		
		if(!experience.containsKey(input)) {
			experience.put(input, new FuzzyValue());
		}
		
		experience.get(input).trainObservation(expected.doubleValue());
		changeData(new DoubleData(experience.get(input).getSignal()));
		
		return getLastOutput();
	}

	@Override
	public INeuron create() {
		return new MultyClassInputSignal(getState().size());
	}

	@Override
	public void trainKid(INeuron kid, Data expected) {
		// NOT supported
	}

	@Override
	public void clearInputs() {
				
	}

}
