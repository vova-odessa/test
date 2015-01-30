package nntest.compute_functions;

import nntest.BaseNeuroLayer;
import nntest.BaseNeuron;
import nntest.interfaces.INeuron;

public interface IComputeFunction {
	/**
	 * Computes aggregated value for all inputs
	 * @param data
	 * @throws Exception
	 */
	public void compute(INeuron data) throws Exception;
	
	/**
	 * Compute modified value for indexed input.
	 * @param neuron
	 * @param index
	 * @throws Exception
	 */
	public void compute(INeuron neuron, int index) throws Exception;
	
	public void train(INeuron data, double correctedValue);
}
