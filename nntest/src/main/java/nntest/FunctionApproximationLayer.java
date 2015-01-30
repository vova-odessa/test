package nntest;

import java.util.ArrayList;

import nntest.compute_functions.IComputeFunction;
import nntest.interfaces.*;

public class FunctionApproximationLayer extends BaseNeuroLayer {
	public FunctionApproximationLayer(BaseNeuroLayer layerForApproximation, IComputeFunction approximator) throws Exception {
		super(new ArrayList<INeuron>());
		
		if(layerForApproximation == null) {
			throw new Exception("Invalid layer for approximation");
		}	
		
		for(int i = 0; i < layerForApproximation.neurons.size(); ++ i) {
			BaseNeuron neuron = new BaseNeuron();
			neuron.setId(layerForApproximation.neurons.get(i).getId());
			AddAdditionalNeurons(neuron);
		}
		
		setComputeFunction(approximator);
	}
}
