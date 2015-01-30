package nntest.identifiers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import nntest.BaseNeuron;
import nntest.interfaces.INeuron;

public abstract class IONeuron extends INeuron {
	protected HashSet<INeuron> inputNeurons = new HashSet<INeuron>();
	protected HashSet<INeuron> outputNeurons = new HashSet<INeuron>();

	private int id = BaseNeuron.genId();
	@Override
	public int getId() {
		return id;
	}
	
	@Override
	public void setId(int id) {
		this.id = id;
	}


	@Override
	public boolean registerInput(INeuron input) {
		if(input == null) {
			return false;
		}
		
		if(inputNeurons.contains(input)) {
			return true;
		}
		inputNeurons.add(input);
		
		return input.registerOutput(this);
	}

	@Override
	public boolean registerOutput(INeuron output) {
		if(output == null) {
			return false;
		}
		
		outputNeurons.add(output);
		return true;
	}

	@Override
	public void activate() throws Exception {
		// send to outputs
		for (INeuron out : outputNeurons) {
			out.handle(this);
		}
	}
	 
	@Override
	public Set<INeuron> getInputNeurons() {
		return inputNeurons;
	}


}
