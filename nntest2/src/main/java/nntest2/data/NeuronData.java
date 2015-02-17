package nntest2.data;

import nntest2.NeuroBase;
import nntest2.neurons.Neuron;

public class NeuronData extends Data {
	private static final String NEURON_PREFIX = "@neuron=";
	Neuron neuron = null;
	String name = null;
	
	public NeuronData(Neuron neuron) {
		this.neuron = neuron;
	}
	
	public NeuronData(String neuron) {
		constructByName(neuron);
	}

	private void constructByName(String neuron) {		
		boolean validate = false;
		neuron = neuron.trim();
		
		while(neuron.startsWith("(") && neuron.endsWith(")")) {
			validate = true;
			neuron = neuron.substring(1, neuron.length() - 1).trim();
		}
		
		if(neuron.startsWith(NEURON_PREFIX)) {
			neuron = neuron.substring(NEURON_PREFIX.length()).trim();
		} 
		
		if( hasSpecial(neuron) ) {
			return;
		}
		
		name = neuron;
		
		this.neuron = NeuroBase.getInstance().findNeuron(new StringData(neuron), false);
		if(this.neuron == null && validate) {
			this.neuron = NeuroBase.getInstance().validateNeuron(new StringData(neuron));
		}		
	}
	
	private static boolean hasSpecial(String data) {
		return data.matches("\\w*[(){}\\[\\]].*");
	}
	
	public NeuronData(StringData neuron) {
		constructByName(neuron.toString());
	}
	
	@Override
	public String toString() {
		if(isValid()) {
			return "(" + NEURON_PREFIX + getOriginalName() + ")";
		} else { 
			return "(" + NEURON_PREFIX + "invalid neuron)";
		}
	}
	
	public boolean isValid() {
		return neuron != null; 
	}
	
	public String getName() {
		if(isValid()) {
			return neuron.getName().toString();
		} else {
			return name;
		}
	}
	
	public String getOriginalName() {
		if(name != null) {
			return name;
		} else if(neuron != null) {
			return neuron.getName().toString();
		}
		
		return null;
	}
	
	public Neuron getNeuron() {
		return neuron;
	}
	
	
}

