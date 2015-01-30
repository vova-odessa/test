package nntest2.neurons;

import java.util.ArrayList;
import java.util.HashMap;

import com.sec.gb.ipa.ks.common.util.Common;

import nntest2.Analyser;
import nntest2.NeuroBase;
import nntest2.data.ArrayData;
import nntest2.data.Data;
import nntest2.data.StringData;
import nntest2.herpers.CommonHelper;

public class InvariantNeuronAnalysingNeuron extends Neuron {
	private static final String NAME = "get invariant neurons";
	
	public InvariantNeuronAnalysingNeuron() {
		super(NAME);
	}
	
	@Override
	public boolean canAnalyseResults() {
		return true;
	}
	
	@Override
	public void compute(Neuron inputNeuron, StringData operator, ArrayList<Data> input, Data output) {
		/**
		 * 1st algorithm
		 * Find all neurons 'f', that can be used for invariant-base F computation: where 'f' is an 'z invariant-value function' for f(input + output) = z  
		 */
		// f - inputNeuron
		// x = input: 0 ... n - 2
		// y = input: n - 1 (last)
		// p = operator
		// z = output
		// find g[f,p](X, Z)
		
		Neuron f = inputNeuron;
		ArrayList<Data> x = ArrayData.subdata(input, 0, input.size() - 1);
		Data y = input.get(input.size() - 1);
		Data z = output;
		
		// test regular neurons
		HashMap<Data, Neuron> neurons = NeuroBase.getInstance().getNeurons();
		
		for (Neuron testNeuron : neurons.values()) {
			Data result = testNeuron.compute(Analyser.EQ_OPER, CommonHelper.mergeCopy(x, z));
			if(result != null && result.compareTo(y) == 0) {
				
			}
		}		
		// TODO test parameterized neurons
		
		// TODO test f1 o ... o fn combinations
	}

}
