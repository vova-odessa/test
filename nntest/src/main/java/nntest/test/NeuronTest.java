package nntest.test;

import java.util.Date;

import org.apache.log4j.Logger;

import nntest.BaseNeuralNetwork;
import nntest.BaseNeuroLayer;
import nntest.Util;
import nntest.data.Data;
import nntest.data.DoubleData;
import nntest.data.NeuronData;
import nntest.interfaces.INeuron;

public class NeuronTest {
	private static final int TRAIN_ITERATIONS = 3;
	Logger logger = Logger.getLogger(NeuronTest.class);
	INeuron classifier = null;
	BaseNeuralNetwork network = null;
	
	public NeuronTest(INeuron input) {
		classifier = input;
	}
	
	public NeuronTest(BaseNeuralNetwork input) {
		network = input;
	}
	
	double TP;
	double FP;
	double TN;
	double FN;
	double E = 0.0000001;
	
	double averageError = 0.0;
	
	public boolean test(double[][] trainData, double [][] trainValidation, double [][]testInput, double [][] testValidation) {
		TP = 0;
		FP = 0;
		TN = 0;
		FN = 0;
		averageError = 0.0;
		
		if(network == null) {
			try {
				network = new BaseNeuralNetwork(trainData[0].length, 1);
				network.AddAdditionalNeurons(classifier);
			} catch (Exception e) {
				return false;
			}
		}		
		
		long trainTime = new Date().getTime();
		
		for(int t = 0; t < TRAIN_ITERATIONS; ++ t) {
			network.clearInputs();
			for(int i = 0; i < trainData.length; ++ i) {
				if(i % 10 == 0) {
					logger.info("Train : " + i + "/" + trainData.length);
				}
				if(trainData[i] == null) {
					network.clearInputs();
				} else {
					network.train(trainData[i], trainValidation[i]);
				}
			}
		}
		
		trainTime = -trainTime + new Date().getTime();
		int fakeNum = 0;
		
		for(int i = 0; i < testInput.length; ++ i) {
			try {
				if(testInput[i] == null) {
					network.clearInputs();
					++ fakeNum;
					continue;
				} else {
					network.compute(testInput[i]);
				}
			} catch (Exception e) {
				++ fakeNum;
				logger.warn(e.getMessage());
				continue;
			}
			double error = network.getError(testValidation[i]);
			averageError += error;
			//double sum = Util.sum(testValidation[i]);
			
			Data data = null;
			if(classifier != null) {
				data = classifier.getLastOutput();
			} else {
				try {
					data = network.getLastOutput();
				} catch(Throwable t) {
				}
			}
			boolean isEmptyAnswer = false;
			
			if(data != null) {
				if(data instanceof DoubleData) {
					if(Math.abs(data.doubleValue()) < E) {
						isEmptyAnswer = true;
					}
				} else if(data instanceof NeuronData) {
					INeuron neuron = ((INeuron)data.getData());
					isEmptyAnswer =  (neuron == null);
				}
			}
			
			if(error > 0) {				
				if(isEmptyAnswer) {
					FN += 1;
				} else {
					if(error == 1) {
						FP += 1;
					} else {					
						FP += error;
						TP += 1 - error;
					}
				}				
			} else {
				if(isEmptyAnswer) {
					TN += 1;
				} else {
					TP += 1;
				}				
			}
		}
		
		averageError /= (testInput.length - fakeNum);
		
		logger.info("Average error = " + averageError);
		logger.info(new String().format("Accuracy = %.3f", accuracy()));
		logger.info("Layer, train: " + trainTime/1000.0 + "s");
		logger.info("\tLayer, handling: " + BaseNeuroLayer.times[0]/1000.0 + "s");
		logger.info("\tLayer, computing: " + BaseNeuroLayer.times[1]/1000.0 + "s");
		
		return true;
	}
	
	public double accuracy() {
		logger.info("TP = " + TP + "; TN = " + TN + "; FP = " + FP + "; FN = " + FN);
		double all = TP + TN + FP + FN;
		if(all > 0) {
			return (double)(TP + TN)/all;
		} else {
			return -1;
		}
	}
	
	public double getAverageError() {
		return averageError;
	}
}
