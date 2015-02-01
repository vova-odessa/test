package nntest2.neurons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.sec.gb.ipa.ks.common.util.AsyncCounter;
import com.sec.gb.ipa.ks.common.util.Common;

import nntest2.Analyser;
import nntest2.NeuroBase;
import nntest2.data.AnalysisRequest;
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
	
	Set<AnalysisRequest> queue = new HashSet<AnalysisRequest>();
	Set<AnalysisRequest> history = new HashSet<AnalysisRequest>();
	AsyncCounter isInAnalysis = new AsyncCounter();
	
	
	@Override
	public void compute(AnalysisRequest input) {
		synchronized (isInAnalysis) {
			if(!isInAnalysis.isEmpty()) {
				if(!history.contains(input)) {
					
					synchronized (queue) {
						queue.add(input);
					}
					history.add(input);					
				}
				return;
			} else {
				isInAnalysis.inc();
			}
		}	
		
		/**
		 * 1st algorithm
		 * Find all neurons 'f', that can be used for invariant-base F computation: where 'f' is an 'z invariant-value function' for f(input + output) = z  
		 */
		// f - inputNeuron
		// x = input: 0 ... n - 2
		// y = input: n - 1 (last)
		// p = operator
		// z = output
		// find g[f,p](X, Z) = Y = F(X), Z = f(X, Y)
		
		Neuron f = input.inputNeuron;
		ArrayList<Data> x = ArrayData.subdata(input.input, 0, input.input.size() - 1);
		Data y = input.input.get(input.input.size() - 1);
		Data z = input.output;
		
		// test regular neurons
		HashMap<Data, Neuron> neurons = NeuroBase.getInstance().getNeurons();
		
		for (Neuron testNeuron : neurons.values()) {
			Data result = testNeuron.compute(Analyser.EQ_OPER, CommonHelper.mergeCopy(x, z));
			if(result != null && result.compareTo(y) == 0) {
				
			}
		}		
		// TODO test parameterized neurons
		
		// TODO test f1 o ... o fn combinations
		
		synchronized (isInAnalysis) {
			synchronized (queue) {
				if(queue.size() > 0) {
					compute(queue.iterator().next());
				} else {
					isInAnalysis.dec();
				}
			}
			
		}
	}

}
