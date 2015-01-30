package nntest;

import java.util.ArrayList;
import java.util.HashMap;

import nntest.compute_functions.MaxAggregatorFunction;
import nntest.compute_functions.SigmoidFunction;
import nntest.compute_functions.MaxAggregatorFunction.NormalzeStrategy;
import nntest.identifiers.MultyClassInputSignal;
import nntest.interfaces.INeuron;

public class ClassBasedInputNetwork extends BaseNeuralNetwork{
	
	private HashMap<INeuron, INeuron> mapInputs = new HashMap<>();
	private int historyLen;

	public ClassBasedInputNetwork(int differentInputNumber, int historyLength) throws Exception {
		super(/*makeNeurons(differentInputNumber, historyLength)*/new ArrayList());
		globalOutput = new BaseNeuron(differentInputNumber, historyLength);
		globalOutput.setComputeFunction(new SigmoidFunction(0.5, 1.0));
		this.historyLen = historyLength;
		
		//registerNNOutput(this);
	}
	
	private static ArrayList<INeuron> makeNeurons(int differentInputNumber, int historyLength) {
		ArrayList<INeuron> result = new ArrayList<>();
		for(int i = 0; i < differentInputNumber; ++ i) {
			result.add(new MultyClassInputSignal(historyLength));
		}
		
		return result;
	}
	
	public static ArrayList<INeuron> makeClassificationForest(int differentInputNumber, int historyLength, int numClasses) throws Exception {
		ArrayList<INeuron> out = new ArrayList<>();
		
		for(int i = 0; i < numClasses; ++ i) {
			ClassBasedInputNetwork binaryClassifier = new ClassBasedInputNetwork(differentInputNumber, historyLength);
			binaryClassifier.setId(i);
			out.add(binaryClassifier);
		}
		
		return out;
	}
	
	@Override
	public void handle(INeuron inputActivation) throws Exception {
		if(!mapInputs.containsKey(inputActivation)) {
			MultyClassInputSignal neuron = new MultyClassInputSignal(historyLen);
			mapInputs.put(inputActivation, neuron );
			AddAdditionalNeurons(neuron);
		}
		
		mapInputs.get(inputActivation).handle(inputActivation);
	}
	
}
