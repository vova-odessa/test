package nntest2.data;

import java.util.ArrayList;

import nntest2.neurons.Neuron;

public class AnalysisRequest implements Comparable<AnalysisRequest>{
	public enum CompareType {FULL, INPUT_NUM};
	
	public Neuron inputNeuron;
	public NeuronData operator;
	public ArrayList<Data> input;
	public Data output;
	
	public CompareType type = CompareType.FULL;
	
	public AnalysisRequest(Neuron inputNeuron, NeuronData operator, ArrayList<Data> input, Data output) {
		this.inputNeuron = inputNeuron;
		this.operator = operator;
		this.input = input;
		this.output = output;
	}
	@Override
	public int compareTo(AnalysisRequest o) {
		int res1 = inputNeuron.compareTo(o.inputNeuron);
		if(res1 != 0) {
			return res1;
		}
		
		res1 = operator.compareTo(o.operator);
		if(res1 != 0) {
			return res1;
		}
		
		switch (type) {
		case INPUT_NUM:
			return Integer.compare(input.size(), o.input.size());
		case FULL:
		default:		
			res1 = new ArrayData(input).compareTo(new ArrayData(o.input));
			/*if(res1 != 0) {
				return res1;
			}
			
			return output.compareTo(o.output);*/
			return res1;
		}	
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("(AR: ");
		if(inputNeuron != null) {
			builder.append(inputNeuron.getName());
		}
		
		if(input != null) {
			builder.append(" " + input.toString());
		}
		
		if(operator != null) {
			builder.append(" " + operator.toString());
		}
		
		if(output != null) {
			builder.append(" " + output.toString());
		}
		
		return super.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		return compareTo((AnalysisRequest) obj) == 0;
	}
	
	@Override
	public int hashCode() {
		return inputNeuron.getName().toString().hashCode();
	}
}