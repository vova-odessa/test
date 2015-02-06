package nntest2.neurons;

import java.util.ArrayList;
import java.util.Set;

import nntest2.data.Data;
import nntest2.data.NeuronData;
import nntest2.data.StringData;

public class GroupAnalysisNeuron extends Neuron{
	public static final String NAME = "analyse groups"; 

	public GroupAnalysisNeuron() {
		super(NAME);
	}
	
	@Override
	public boolean canAnalyseNeurons() {
		return true;
	}
	
	@Override
	public boolean canAnalyseResults() {
		return true;
	}
	
	@Override
	public Set<NeuronData> compute(Neuron inputNeuron) {
		return super.compute(inputNeuron);
	}
	
	@Override
	public void compute(Neuron inputNeuron, NeuronData operator, ArrayList<Data> input, Data output) {
		super.compute(inputNeuron, operator, input, output);
	}
}
