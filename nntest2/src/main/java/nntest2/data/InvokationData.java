package nntest2.data;

import java.util.ArrayList;
import java.util.HashMap;

import nntest2.Analyser;

public class InvokationData {
	public NeuronData neuron = null;
	public ArrayList<Data> inputMapping = null;
	
	public InvokationData(NeuronData neuron, ArrayList<Data> inputMapping) throws Exception {
		if(neuron == null || inputMapping == null) {
			throw new Exception();
		}
		this.neuron = neuron;
		this.inputMapping = inputMapping;
	}
	
	public Data invoke(ArrayList<Data> parentMethodInput, HashMap<Data, Data> context) {
		ArrayList<Data> realInput = resolveInput(parentMethodInput, context);
		return neuron.getNeuron().compute(Analyser.EQ_OPER, realInput);
	}
	
	public ArrayList<Data> resolveInput(ArrayList<Data> parentMethodInput, HashMap<Data, Data> context) {		
		if(inputMapping == null) {
			return null;
		}
		ArrayList<Data> result = new ArrayList<Data>();
		
		for(int i = 0, len = inputMapping.size(); i < len; ++ i) {
			Data curr = inputMapping.get(i);
			if(curr instanceof IndexData) {
				int ind = ((IndexData) curr).index;
				if(ind >= 0 && parentMethodInput.size() > ind) {
					try {
						result.add(parentMethodInput.get(ind));
					} catch(Throwable t) {
						if(neuron != null) {
							neuron.getNeuron().getLogger().error("InvokationData: No element with index [" + ind + "] in parent input: " + t.getMessage());
						}
						return null;
					}
				} else {
					if(neuron != null) {
						neuron.getNeuron().getLogger().error("InvokationData: No element with index [" + ind + "] in parent input");
					}
					return null;
				}
			} else {
				result.add(curr);
			}
		}
		
		return result;
	}
}
