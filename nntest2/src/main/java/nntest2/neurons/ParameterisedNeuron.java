package nntest2.neurons;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import nntest2.data.ArrayData;
import nntest2.data.ComputationStatistic.DataPredicate;
import nntest2.data.Data;
import nntest2.data.IndexData;
import nntest2.data.NeuronData;
import nntest2.data.StringData;

public class ParameterisedNeuron extends Neuron {
	private static final String VAR_PREFIX = "n";
	Data[] operatorPars = null; 
	
	//
	TreeMap<Integer, HashMap<Neuron, Data>> relations = null;
	
	//
	TreeMap<Data, TreeSet<Integer> > internalRelations = null;
	
	//
	TreeMap<Integer, Integer> index = null;
	Neuron parentNeuron = null;

	public ParameterisedNeuron(Data[] operPars, TreeMap<Integer, HashMap<Neuron, Data>> relations, TreeMap<Data, TreeSet<Integer> > internalRelations, Neuron parentNeuron) throws Exception {
		super("");
		
		if(parentNeuron == null || operPars == null || operPars.length < 1 || relations == null || relations.isEmpty() || internalRelations == null || internalRelations.isEmpty() ) {
			throw new Exception("Can not make neuron. Bad data");
		}
		
		this.operatorPars = operPars;
		this.relations = relations;
		this.internalRelations = internalRelations;
		
		index = new TreeMap<>();
		
		int counter = 0;
		
		for(Entry<Data, TreeSet<Integer>> data: internalRelations.entrySet()) {
			
			for(Integer indexes : data.getValue()) {
				index.put(indexes, counter);
			}
			
			++counter;
		}
		
		this.parentNeuron = parentNeuron;
		
		setName(makeName());		
	}
	
	private String makeName() {
		int counter = 0;
		
		StringBuilder nameBuilder = new StringBuilder();
		
		for (Data part : operatorPars) {
			
			if(nameBuilder.length() > 0) {
				nameBuilder.append(" ");
			}
			
			if(index.containsKey(counter)) {
				nameBuilder.append(VAR_PREFIX).append(index.get(counter));
			} else {
				nameBuilder.append(part);
			}
			
			++ counter;
		}
		return nameBuilder.toString();
	}
	
	@Override
	public boolean canValidate() {
		return true;
	}
	
	@Override
	public boolean validate(String name) {		
		Data[] nameParts = Data.construct(name).elements();
		
		TreeMap<Integer, Data> foundData = mapData(nameParts);
		
		// that verification need to check theather this operator differs from simple one. If no, no need to use it.
		return foundData != null && foundData.size() > 0;
	}

	private TreeMap<Integer, Data> mapData(Data[] nameParts) {		
		if(nameParts.length != operatorPars.length) {
			return null;
		}
		
		TreeMap<Integer, Data> foundData = new TreeMap<>();
		
		int len = nameParts.length;
		for(int ind = 0; ind < len; ++ ind) {
			if(index.containsKey(ind)) {
				int varNum = index.get(ind);
				Data currData = nameParts[ind];
				
				if(foundData.containsKey(varNum)) {
					if(foundData.get(varNum).compareTo(currData) != 0) {
						// not compatible data.
						return null;
					}
				} else {
					foundData.put(varNum, currData);
				}
			} else if( nameParts[ind].compareTo(operatorPars[ind]) != 0 ) {
				return null;
			}
		}
		return foundData;
	}
	
	@Override
	public Data compute(NeuronData operatorName, ArrayList<Data> input) {
		// check memory
		Data result = super.compute(operatorName, input);
		
		if(result != null) {
			return result;
		}
		
		return computeEx(operatorName, input);
	}
	
	@Override
	public Data computeEx(NeuronData operatorName, ArrayList<Data> input) {
		String originalName = operatorName.getOriginalName();
		
		if(originalName == null) {
			return null;
		}
		
		Data[] nameParts = //ArrayData.splitToElements(operatorName.toString());
				ArrayData.splitToElements(originalName);
		if(nameParts == null) {
			return null;
		}
		
		TreeMap<Integer, Data> foundData = mapData(nameParts);
		
		if(foundData == null ) {
			return null;
		}
		
		Set<Integer> configuredVariables = new HashSet<>();
		
		HashMap<Neuron, HashMap<Data, Data> > configuration = new HashMap<>();
		
		int len = nameParts.length;		
		for(int ind = 0; ind < len; ++ ind) {
			if( index.containsKey(ind) ) {
				int varName = index.get(ind);
				
				// variable can be defined several times, but need to be configured once
				if(!configuredVariables.contains(varName)) {
					configuredVariables.add(varName);
										
					if(relations.containsKey(ind)) {
						if(foundData.containsKey(varName)) { 
							// if there data mapped for that position
							for( Entry<Neuron, Data> rels: relations.get(ind).entrySet() ) {
								if( foundData.containsKey(varName) ) {
									//configuration.put(rels.getKey(), foundData.get(varName));
									
									if(!configuration.containsKey(rels.getKey())) {
										configuration.put(rels.getKey(), new HashMap<Data, Data>());
									}
									configuration.get(rels.getKey()).put(rels.getValue(), foundData.get(varName));
								}
							}
						} else {
							// means the value is regular. No need to change
						}
					} else {
						// situation is not possible. Something wrong happened.
						getLogger().error("Failed to find configuration for " + ind + " part of operator");
						return null;
					}
				}
			}
		}
		
		return parentNeuron.computeEx(operatorName, input, configuration);
	}
	
	@Override
	public Data computeEx(NeuronData operatorName, ArrayList<Data> input, HashMap<Neuron, HashMap<Data, Data>> alternativeData) {
		// For that neuron alternative data should be recomputed on place depend on operator name 
		return computeEx(operatorName, input);
	}
	
	
	public static ArrayList<Data> replaceData(ArrayList<Data> data, HashMap<Data, Data> candidate) {
		ArrayList<Data> copy = new ArrayList<>(data);
		/*if(copy.size() > 0) {
			copy.set(copy.size() - 1, candidate);
		}*/
		
		// replace direct matches first
		for(int i = 0; i < copy.size(); ++ i) {
			if(! (copy.get(i) instanceof IndexData) && candidate.containsKey(copy.get(i))) {
				copy.set(i, candidate.get(copy.get(i)));
			}
		}
		
		// replace indexed matches
		for(int i = 0; i < copy.size(); ++ i) {
			IndexData indexKey = new IndexData(i);
			
			if((copy.get(i) instanceof IndexData) ) {
				if(candidate.containsKey(indexKey)) {
					copy.set(i, candidate.get(indexKey));
				}
				
				if(((IndexData)copy.get(i)).index == DataPredicate.OUTPUT_INDEX) {
					IndexData outputIndex = new IndexData(DataPredicate.OUTPUT_INDEX);
					
					if(candidate.containsKey(outputIndex)) {
						copy.set(i, candidate.get(outputIndex));
					}
				}
			}
		}
			
		
		
		return copy;
	}
}
