package nntest2.data;

import nntest2.NeuroBase;
import nntest2.neurons.Neuron;

public class NeuronData extends Data {
	private static final String NEURON_PREFIX = "(@neuron=";
	Neuron neuron = null;
	
	public NeuronData(Neuron neuron) {
		this.neuron = neuron;
	}
	
	public NeuronData(String neuron) {
		if(neuron.startsWith(NEURON_PREFIX)) {
			neuron.substring(NEURON_PREFIX.length(), neuron.length() - 1);
		}
		
		this.neuron = NeuroBase.getInstance().findNeuron(new StringData(neuron), false);
	}
	
	@Override
	public String toString() {
		return NEURON_PREFIX + neuron.getName() + ")";
	}
	
	public boolean isValid() {
		return neuron != null; 
	}
	
	public Neuron getNeuron() {
		return neuron;
	}
}
