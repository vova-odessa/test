package nntest.identifiers;

import java.util.ArrayList;
import java.util.HashMap;

import nntest.BaseNeuron;
import nntest.data.Data;
import nntest.data.DataNeuron;
import nntest.interfaces.INeuron;

public class PrecomputeBaseNeuron extends BaseNeuron {
	INeuron precomputer = null;
	INeuron dataNeuron = new DataNeuron();
	
	HashMap<INeuron, INeuron> inputPrecomputers = new HashMap<>();
	
	public PrecomputeBaseNeuron(INeuron precomputer)throws Exception {
		if(precomputer == null) {
			throw new NullPointerException("precomputer can not be null");
		}
		 
		this.precomputer = precomputer;
	}
	
	public PrecomputeBaseNeuron(int neuronStateMemory, int inputMemoryNumber, INeuron precomputer) throws Exception {
		super(neuronStateMemory, inputMemoryNumber);
		
		this.precomputer = precomputer;
	}
	
	@Override
	public void handle(INeuron inputn) throws Exception {
		ArrayList<Data> input = inputn.getState();
		
		INeuron precompute = null;
		
		if(inputPrecomputers.containsKey(inputn)) {
			precompute = inputPrecomputers.get(inputn);
		} else {
			precompute = precomputer.create();
			inputPrecomputers.put(inputn, precompute);
		}
		
		for (Data data : input) {
			dataNeuron.setId(inputn.getId());
			dataNeuron.addData(data);
			precompute.handle(dataNeuron);
			precompute.compute();
			data.set(precompute.getLastOutput());
		}
		
		super.handleForData(inputn, input);
	}
	
	@Override
	public void trainKid(INeuron kid, Data expected) {
		INeuron precompute = null;
		
		if(inputPrecomputers.containsKey(kid)) {
			precompute = inputPrecomputers.get(kid);
		} else {
			logger.error("Precomputer not exist. Can not train");
			return;
		}
		precompute.train(expected);
	}
	
	@Override
	public INeuron create() {
		try {
			return new PrecomputeBaseNeuron(getState().size(), inputDataPerNeuronNumber, precomputer);
		} catch (Exception e) {
			logger.error("Failed to create PrecomputeBaseNeuron: " + e.getMessage());
		}
		return null;
	}
}
