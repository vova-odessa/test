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
import nntest2.data.AnalysisRequest.CompareType;
import nntest2.data.ArrayData;
import nntest2.data.Data;
import nntest2.data.IndexData;
import nntest2.data.InvokationData;
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
	
	Map<AnalysisRequest, Set<Neuron>> analysed = new HashMap();
	Map<AnalysisRequest, Map<Neuron, Boolean>> relationsState = new HashMap<>();
 	
	
	@Override
	public void compute(AnalysisRequest input) {
		if(/*!history.contains(input)*/!queue.contains(input)) {
			
			synchronized (queue) {
				queue.add(input);
			}
			//history.add(input);	
			
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
			
			AnalysisRequest historyInput = new AnalysisRequest(input.inputNeuron, input.operator, input.input, input.output);
			historyInput.type = CompareType.INPUT_NUM;
			
			if(!analysed.containsKey(input)) {
				analysed.put(input, new HashSet<Neuron>());				
			}
			
			if(!relationsState.containsKey(historyInput)) {
				relationsState.put(historyInput, new HashMap<Neuron, Boolean>());
			}
			Set<Neuron> analysedRelations = analysed.get(input);
			Map<Neuron, Boolean> historyForNeuron = relationsState.get(historyInput);
			
			// test regular neurons
			HashMap<Data, Neuron> neurons = NeuroBase.getInstance().getNeurons();
			Set<InvokationData> invocations = new HashSet<>();
			
			for (Neuron testNeuron : neurons.values()) {
				if(analysedRelations.contains(testNeuron)) {
					continue;
				} 
				
				if( x.size() == 0 && y.compareTo(z) == 0 ) {
					continue;
				}
				
				analysedRelations.add(testNeuron);
				
				if(historyForNeuron.containsKey(testNeuron) && historyForNeuron.get(testNeuron) == false) {
					// means for some configuration found that they are not bijective
					// just skip 
					continue;
				}
				
				Boolean lastState = historyForNeuron.get(testNeuron);
				
				Data yRes = testNeuron.compute(new NeuronData(Analyser.EQ_OPER), CommonHelper.mergeCopy(x, z));
				if(yRes != null && yRes.compareTo(y) == 0) {
					ArrayList<Data> inputMapping = new ArrayList<Data>();
					int ind = 0;
					for(; ind < x.size(); ++ ind) {
						inputMapping.add(new IndexData(ind));
					}
					inputMapping.add(new IndexData(ind));
					getLogger().info("!!!!! " + f.getName() + "(" + x.toString() + ", " + y + ")->" + testNeuron.getName() + "(" + x.toString()+  ", " + z + ")");
					try {
						invocations.add(new InvokationData(new NeuronData(testNeuron), inputMapping));
						historyForNeuron.put(testNeuron, true);
					} catch (Exception e) {
						historyForNeuron.put(testNeuron, false);
					}
				} else {
					//getLogger().error("!! " + f.getName() + "->" + testNeuron.getName() + ".. " + y + ".. " + yRes);
					historyForNeuron.put(testNeuron, false);
				}
				
				if(lastState != null && historyForNeuron.get(testNeuron) == false) {
					// remove previous wrong data
					//f.removeRelation(NeuroBase.getInstance().bijection, testNeuron);
					//f.getLogger().info("REMOVED: " + f.getName() + "->" + testNeuron.getName());
				}
			}		
			
			if(invocations.size() > 0) {
				//getLogger().info(f.getName() + ":found " + invocations.size());
				f.addRelations(NeuroBase.getInstance().bijection, invocations);
			} else {
				//getLogger().info("");
			}
			// TODO test parameterized neurons
			
			// TODO test f1 o ... o fn combinations
		}
		
		isInAnalysis.dec();
	}

}
