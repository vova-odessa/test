package nntest.identifiers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import com.sec.gb.ipa.ks.common.util.AsyncCounter;

import nntest.data.Data;
import nntest.data.DoubleData;
import nntest.interfaces.INeuron;

public abstract class ANeuronIdentifier extends IONeuron {

	
	protected LinkedList<Data> state = new LinkedList<Data>();
	
	protected HashMap<INeuron, Data> inputData = new HashMap<>();
	
	public ANeuronIdentifier() {
		init(1);
	}
	
	public ANeuronIdentifier(int memorySize) {
		init(memorySize);
	}

	private void init(int memorySize) {
		for(int i = 0; i < memorySize; ++i) {
			state.add(new DoubleData(0));
		}
	}

	@Override
	public void handle(INeuron inputActivation) throws Exception {
		inputData.put(inputActivation, inputActivation.getLastOutput());
	}


	@Override
	public Data getLastOutput() {
		return state.getFirst();
	}

	@Override
	public boolean supportTraining() {
		return true;
	}

	@Override
	public ArrayList<Data> getState() {
		return new ArrayList<>(state);
	}
	
	@Override
	public int compareTo(INeuron o) {
		if(o == null) {
			return 1;
		}
		return new Integer(getId()).compareTo(new Integer(o.getId()));
	}
	
	@Override
	public boolean addData(Data data) {
		state.addFirst(data);
		state.removeLast();
		
		return true;
	}
	
	@Override
	public boolean changeData(Data data) {
		state.getFirst().set(data.doubleValue());
		
		try {
			activate();
		} catch (Exception e) {
		}
		
		return true;
	}
	
	@Override
	public void clearInputs() {
		if(!isLocked()) {
			lock(true);
			for (INeuron neuron : inputNeurons) {
				neuron.clearInputs();
			}
			
			// self not require cleaning
			for(int i = 0; i < state.size(); ++i) {
				state.set(i, new DoubleData(0));
			}
		}
	}
	
	@Override
	public void lockAll(boolean state) {
		if(state == true && isLocked()) {
			return;
		}
		
		lock(state);
		
		for(INeuron input: inputNeurons) {
			input.lockAll(state);
		}
	}
}
