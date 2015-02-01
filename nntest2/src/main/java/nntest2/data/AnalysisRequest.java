package nntest2.data;

import java.util.ArrayList;

import nntest2.neurons.Neuron;

public class AnalysisRequest implements Comparable<AnalysisRequest>{
	public Neuron inputNeuron;
	public StringData operator;
	public ArrayList<Data> input;
	public Data output;
	public AnalysisRequest(Neuron inputNeuron, StringData operator, ArrayList<Data> input, Data output) {
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
		
		res1 = new ArrayData(input).compareTo(new ArrayData(o.input));
		if(res1 != 0) {
			return res1;
		}
		
		return output.compareTo(o.output);
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
}