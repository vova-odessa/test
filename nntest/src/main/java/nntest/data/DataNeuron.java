package nntest.data;

import java.util.ArrayList;

import nntest.identifiers.IONeuron;
import nntest.interfaces.INeuron;

/**
 * Data neuron with out memory.
 *  
 * @author sec
 *
 */
public class DataNeuron extends IONeuron {
	
	Data state = null;

	@Override
	public int compareTo(INeuron o) {
		return state.compareTo(o.getLastOutput());
	}

	@Override
	public void handle(INeuron inputActivation) throws Exception {
		state = inputActivation.getLastOutput();
	}

	@Override
	public void compute() throws Exception {
		// DO NOTHING
	}

	@Override
	public Data getLastOutput() {
		return state;
	}

	@Override
	public Data train(Data expected) {
		// NOT SUPPORTED to train raw input data
		return state;
	}

	@Override
	public void lock(boolean state) {
	}

	@Override
	public boolean supportTraining() {
		return false;
	}

	@Override
	public ArrayList<Data> getState() {
		ArrayList<Data> out = new ArrayList<>();
		out.add(state);
		return out;
	}

	@Override
	public boolean addData(Data data) {
		// alternative input
		state = data;
		return true;
	}

	@Override
	public boolean changeData(Data data) {
		// alternative input
		state = data;
		return true;
	}

	@Override
	public INeuron create() {
		return new DataNeuron();
	}

	@Override
	public void trainKid(INeuron kid, Data expected) {
		// not supported
	}

	@Override
	public void clearInputs() {
		state = null;		
	}
	
	@Override
	public void lockAll(boolean state) {
		if(state == true && isLocked()) {
			return;
		}
		
		lock(state);
	}
}
