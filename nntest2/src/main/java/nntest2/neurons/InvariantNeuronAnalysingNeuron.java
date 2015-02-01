package nntest2.neurons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sec.gb.ipa.ks.common.util.AsyncCounter;
import com.sec.gb.ipa.ks.common.util.Common;

import nntest2.Analyser;
import nntest2.NeuroBase;
import nntest2.data.AnalysisRequest;
import nntest2.data.ArrayData;
import nntest2.data.Data;
import nntest2.data.IndexData;
import nntest2.data.NeuronData;
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
	
	Map<Neuron, Set<Neuron>> analysed = new HashMap();
	
	
	@Override
	public void compute(AnalysisRequest input) {
		if(!history.contains(input)) {
			
			synchronized (queue) {
				queue.add(input);
			}
			history.add(input);	
			
			if(isInAnalysis.isEmpty()) {
				isInAnalysis.inc();
				analysisFunc();
			}					
		}	
	}
	
	private void analysisFunc() {
		while(queue.size() > 0) {
			AnalysisRequest input = null;
			
			synchronized (queue) {
				input = queue.iterator().next();
				queue.remove(input);
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
			// find g[f,p](x, z) = y = F(X), z = f(x, y)
			
			Neuron f = input.inputNeuron;
			ArrayList<Data> x = ArrayData.subdata(input.input, 0, input.input.size() - 1);
			Data y = input.input.get(input.input.size() - 1);
			Data z = input.output;
			
			if(!analysed.containsKey(f)) {
				analysed.put(f, new HashSet<Neuron>());
			}
			Set<Neuron> analysedRelations = analysed.get(f);
			
			// test regular neurons
			HashMap<Data, Neuron> neurons = NeuroBase.getInstance().getNeurons();
			
			for (Neuron testNeuron : neurons.values()) {
				if(analysedRelations.contains(testNeuron)) {
				//	continue;
				} 
				
				analysedRelations.add(testNeuron);
				
				Data yRes = testNeuron.compute(Analyser.EQ_OPER, CommonHelper.mergeCopy(x, z));
				if(yRes != null && yRes.compareTo(y) == 0) {
					ArrayList<Data> inputMapping = new ArrayList<Data>();
					inputMapping.add(new IndexData(0));
					inputMapping.add(new IndexData(1));
					//getLogger().error("!!!!! " + f.getName() + "->" + testNeuron.getName());
				}
			}		
			// TODO test parameterized neurons
			
			// TODO test f1 o ... o fn combinations
		}
		
		isInAnalysis.dec();
	}

}
