package nntest;

import java.util.ArrayList;
import java.util.TreeMap;

import org.apache.log4j.Level;

import nntest.compute_functions.BaseComputeFunction;
import nntest.data.Data;
import nntest.data.DoubleData;
import nntest.identifiers.MaxInputSelector;
import nntest.interfaces.INeuron;

public class BaseNeuralNetwork extends BaseNeuroLayer {

	INeuron globalOutput = new MaxInputSelector();
	protected ArrayList<BaseNeuroLayer> layers = new ArrayList<>();
	protected boolean[] delayMap = null;
	
	public BaseNeuralNetwork(int defferentInputNumber, int historyLength) throws Exception {
		super(defferentInputNumber, historyLength);
	}
	
	public BaseNeuralNetwork(ArrayList<INeuron> neurons) throws Exception {
		super(neurons);
		isInputLayer = true;
	}
	
	protected void registerNNOutput(BaseNeuroLayer output) {
		output.registerOutput(globalOutput);
		
		for (INeuron neuron : output.neurons) {
			globalOutput.registerInput(neuron);
		}
	}
	
	/**
	 * Class id output expected as double, other as 0
	 * @param classId
	 * @param value
	 */
	public void applyVerification(int classId, double value) {
		for(INeuron neuron: globalOutput.getInputNeurons()) {
			if(neuron instanceof BaseNeuron) {
				((BaseNeuron) neuron).lock(true);
			}
		}
		for(INeuron neuron: globalOutput.getInputNeurons()) {
			if(neuron.getId() == classId) {
				neuron.train(new DoubleData(value));
			} else {
				neuron.train(new DoubleData(0));
			}
		}
		for(INeuron neuron: globalOutput.getInputNeurons()) {
			if(neuron instanceof BaseNeuron) {
				((BaseNeuron) neuron).lock(false);
			}
		}
	}
	
	/**
	 * All output expected as double, other as 0
	 * @param classId
	 * @param value
	 * @return 
	 */
	public void applyVerification(double value) {
		for(INeuron neuron: globalOutput.getInputNeurons()) {
			neuron.lock(true);
		}
		for(INeuron neuron: globalOutput.getInputNeurons()) {
			neuron.train(new DoubleData(value));
		}
		for(INeuron neuron: globalOutput.getInputNeurons()) {
			neuron.lock(false);
		}
	}
	
	public void applyVerification(TreeMap<Integer, Double> classIds) {
		for(INeuron neuron: globalOutput.getInputNeurons()) {
			neuron.lock(true);
		}
		for(INeuron neuron: globalOutput.getInputNeurons()) {
			neuron.train(new DoubleData(classIds.get(neuron.getId())));
		}
		for(INeuron neuron: globalOutput.getInputNeurons()) {
			neuron.lock(false);
		}
	}
	
	public void applyVerification(double[] classIds) {
		for(INeuron neuron: globalOutput.getInputNeurons()) {
			neuron.lock(true);
		}
		
		boolean isClassInput = ((classIds.length == 1)&&globalOutput.getInputNeurons().size() > 1);
		
		for(INeuron neuron: globalOutput.getInputNeurons()) {
			if(!isClassInput) {
				if(neuron.getId() < classIds.length) {
					neuron.train(new DoubleData(classIds[neuron.getId()]));
				} else {
					logger.error("Verification of neuron id = " + neuron.getId() + " failed. Not anough input");
				}
			} else {
				if(neuron.getId() == classIds[0]) {
					neuron.train(new DoubleData(1));
				} else {
					neuron.train(new DoubleData(0));
				}
			}
		}
		for(INeuron neuron: globalOutput.getInputNeurons()) {
			neuron.lock(false);
		}
	}
	
	/**
	 * Class id output expected as double, other as 0
	 * @param classId
	 * @param value
	 */
	public double getError(int classId, double value) {
		double err = 0;
		for(INeuron neuron: globalOutput.getInputNeurons()) {
			if(neuron.getId() == classId) {
				err += BaseComputeFunction.errorCriteria(neuron.getLastOutput().doubleValue(), value);
			} else {
				err += BaseComputeFunction.errorCriteria(neuron.getLastOutput().doubleValue(), 0);
			}
		}
		
		return err;
	}
	
	/**
	 * All output expected as double, other as 0
	 * @param classId
	 * @param value
	 * @return 
	 */
	public double getError(double value) {
		double err = 0;
		for(INeuron neuron: globalOutput.getInputNeurons()) {
			err += BaseComputeFunction.errorCriteria(neuron.getLastOutput().doubleValue(), value);
		}
		
		return err;
	}
	
	public double getError(TreeMap<Integer, Double> classIds) {
		double err = 0;
		for(INeuron neuron: globalOutput.getInputNeurons()) {
			err += BaseComputeFunction.errorCriteria(neuron.getLastOutput().doubleValue(), classIds.get(neuron.getId()));
		}
		
		return err;
	}
	
	public double getError(double[] classIds) {
		double err = 0;
		
		StringBuilder actual = new StringBuilder();
		StringBuilder expected = new StringBuilder();
		
		boolean isMulticlassVerification = (classIds.length == 1 && globalOutput.getInputNeurons().size() > 1);
		if(isMulticlassVerification) {
			logger.debug("Expected class : " + classIds[0]);
		}
				
		for(INeuron neuron: globalOutput.getInputNeurons()) {
			actual.append(String.format(" %.5f", neuron.getLastOutput().doubleValue()));
			if(!isMulticlassVerification) {
				err += //BaseComputeFunction.errorCriteria(neuron.getLastOutput().doubleValue(), classIds[neuron.getId()]);
						Math.abs(neuron.getLastOutput().doubleValue() - classIds[neuron.getId()])/2;
				expected.append(String.format(" %.5f", classIds[neuron.getId()]));
			} else {
				if( neuron.getId() == classIds[0] ) {
					err += Math.abs(neuron.getLastOutput().doubleValue() - 1)/2;
					expected.append(String.format(" %.5f", 1.0));
				} else {
					err += Math.abs(neuron.getLastOutput().doubleValue())/2;
					expected.append(String.format(" %.5f", 0.0));
				}
			}
		}
		
		logger.setLevel(Level.DEBUG);
		logger.debug("Result :" + actual.toString());
		logger.debug("Expect :" + expected.toString());
		logger.debug(String.format("Error  : %.8f", err));
		 
		return err;
	}
	
	public INeuron compute(double [] inputs) throws Exception {
		if(!addData(inputs) ) {
			throw new Exception("Wrong input ");
		}
		
		if(layers.size() > 0) {
			BaseNeuroLayer last = layers.get(layers.size() - 1);
			if(last.isMissedActivation) {
				last.finalizeActivation();
			}
		}
		
		// check delay map
		if(delayMap != null) {
			for(int i = 0; i < delayMap.length; ++ i) {
				// handle delayed layer 
				if(delayMap[i] && i + 1 < layers.size()) {
					int nInd = 0;
					for(INeuron neuron: neurons) {
						++ nInd;
						if(nInd >= inputNeuronsSize) {
							layers.get(i + 1).handle(neuron);
						}
					}					
				}
			}
		}
		
		super.activate();
		globalOutput.compute();
		
		return globalOutput;		
	}
	
	public void train(double [] inputs, TreeMap<Integer, Double> classIds) {
		try {
			compute(inputs);
			applyVerification(classIds);
		} catch (Exception e) {
			logger.error("BaseNeuralNetwork.train(1): " + e.toString());
		}
	}
	
	public void train(double [] inputs, double[] classIds) {
		try {
			compute(inputs);
			applyVerification(classIds);
		} catch (Exception e) {
			logger.error("BaseNeuralNetwork.train(2): " + e.toString());
		}
	}
	
	public void train(double [] inputs, double value) {
		try {
			compute(inputs);
			applyVerification(value);
		} catch (Exception e) {
			logger.error("BaseNeuralNetwork.train(3): " + e.toString());
		}
	}
	
	
	public void train(double [] inputs, int classId, double value) {
		try {
			compute(inputs);
			applyVerification(classId, value);
		} catch (Exception e) {
			logger.error("BaseNeuralNetwork.train(4): " + e.toString());
		}
	}
	
	@Override
	public Data getLastOutput() {
		return globalOutput.getLastOutput();
	}
	
	@Override
	public void clearInputs() {
		globalOutput.clearInputs();
		globalOutput.lockAll(false);
	}
	
	@Override
	public boolean registerOutput(INeuron output) {
		return globalOutput.registerOutput(output);
	}
	
	public boolean register1stLayerOutput(INeuron output) {
		return super.registerOutput(output);
	}
	
	@Override
	public void compute() throws Exception {
		super.compute();
		super.activate();
		
		globalOutput.compute();
	}
	
	@Override
	public void activate() throws Exception {
		globalOutput.activate();
	}
	
	@Override
	public ArrayList<Data> getState() {
		return globalOutput.getState();
	}
	
	@Override
	public boolean supportTraining() {
		return true;
	}
	
	@Override
	public Data train(Data expected) {
		return globalOutput.train(expected);
	}
	
	@Override
	public boolean AddAdditionalNeurons(INeuron neuron) {
		if(layers.size() == 0) {
			// input should go to global output
			globalOutput.registerInput(neuron);
		}
		return super.AddAdditionalNeurons(neuron);
	}
}
