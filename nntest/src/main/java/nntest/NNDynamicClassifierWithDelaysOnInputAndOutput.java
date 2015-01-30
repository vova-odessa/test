/**
 * @description 
 * @package  nntest 
 * @file 	 NNDynamicClassifierWithDelaysOnInputAndOutput.java
 * @time_creation  2014 Nov 10, 2014
 * @author : Zubariev Volodymyr
 * 
 */
package nntest;

import java.util.ArrayList;

import nntest.interfaces.INeuron;

/**
 * The Class NNDynamicClassifierWithDelaysOnInputAndOutput.
 * 
 * This Neural network is configures Input layer to process with configured delay on inputs, and on output.
 * Above mean configuration of input layer.
 * Internal behavior should be configured by configured internal layers.
 * That can be easy made by inheriting this class, and adding other logic.  
 */
public class NNDynamicClassifierWithDelaysOnInputAndOutput extends BaseNeuralNetwork {
	
	/** The layers. */
	//ArrayList<BaseNeuroLayer> layers;

	/**
	 * Instantiates a new NN dynamic classifier with delays on input and output.
	 * 
	 * @param differentInputNumber The number of input features, if <= 0, exception will throw.
	 * @param delayedInputsNumber The size of each input memory. If = 1, no memory will used, If <= 0, exception will throw.
	 * @param delayedOutputsNumber The size of output memory. If = 0, delayed output will be turned off. If < 0, exception will throw.
	 * @param internalLayers The internal layers, it can not be 0. Neurons from output layer will be added
	 * @throws Exception the exception
	 */
	public NNDynamicClassifierWithDelaysOnInputAndOutput(
			int differentInputNumber, int delayedInputsNumber, 
			int delayedOutputsNumber, boolean[] delayMap,  // that means they are using for several layers, if null, use only for first
			ArrayList<BaseNeuroLayer> internalLayers) throws Exception {
		super(differentInputNumber, delayedInputsNumber);
		
		this.delayMap = delayMap;
		
		if( delayedOutputsNumber < 0 ) {
			logger.error("delayedOutputsNumber < 0. Use 0, to turn it off, or > 0 to use memory on output.");
			throw new Exception("delayedOutputsNumber < 0. Use 0, to turn it off, or > 0 to use memory on output.");
		}
		
		if(internalLayers == null || internalLayers.size() == 0) {
			logger.error("Should be at least 1 internal layer. ");
			throw new Exception("Should be at least 1 internal layer. ");
		}
		
		layers = internalLayers;
		
		BaseNeuroLayer firstLayer = internalLayers.get(0);
		BaseNeuroLayer lastLayer = internalLayers.get(internalLayers.size() - 1);
		
		// assign first layer as input from outside
		register1stLayerOutput(firstLayer);
		registerNNOutput(lastLayer);
		
		// make layer relations
		for(int i = 0; i < internalLayers.size() - 1; ++ i) {
			internalLayers.get(i + 1).registerInput(internalLayers.get(i));
		}
		
		if(delayedOutputsNumber > 0) {
			for(INeuron neuron: lastLayer.neurons) {
				if(neuron instanceof BaseNeuron) {
					// for every out neuron configure memory size and assign as delayed input
					((BaseNeuron) neuron).setNeuronStateMemorySize(delayedOutputsNumber);
					AddAdditionalNeurons(neuron);
				}
			}
		} else {
			for(INeuron neuron: lastLayer.neurons) {
				((BaseNeuron) neuron).setNeuronStateMemorySize(1);
			}
		}
	}
	
	@Override
	public void handle(INeuron inputActivation) throws Exception {
		logger.error("This method is disabled for neural network. Please use Add data, with input # from range specified on NN creation");
		throw new Exception("This method is disabled for neural network. Please use Add data, with input # from range specified on NN creation");
	}
}
