package nntest2.herpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.sec.gb.ipa.ks.common.data.Pair;

import nntest2.NeuroBase;
import nntest2.data.ArrayData;
import nntest2.data.BoolData;
import nntest2.data.ComputationStatistic.RelationsData;
import nntest2.data.Data;
import nntest2.data.IntegerData;
import nntest2.data.NeuronData;
import nntest2.data.StringData;
import nntest2.data.TextData;
import nntest2.neurons.InvariantNeuronAnalysingNeuron;
import nntest2.neurons.Neuron;
import nntest2.neurons.ParameterisedNeuron;

public class NeuroHelper {
	public static final String HAS_SAME_ELEMENTS = "has same elements";
	public static final String BACK_SUBSTRING_FROM_TO = "back substring from to";
	public static final String SUBSTRING_FROM_LEN = "substring from len";
	public static final String SUBSTRING_FROM_TO = "substring from to";
	public static final String FIND_OPERATOR_TO_SOLUTION_RELATIONS = "find operator to solution relations";
	public static final String GET_FROM_END = "get from end";
	public static final String GET_FROM_BEGIN = "get from begin";
	public static final String COUNT = "count";
	public static final String CONTAINS = "contains";
	public static final String INDEX_OF = "index of";
	public static final String BACK_INDEX_OF = "back index of";

	public static HashMap<Data, Neuron> createBaseNeurons(NeuroBase base) {
		HashMap<Data, Neuron> result = new HashMap<>();
		
		// COUNT: that is elementary observation fact as visual measure of group, 
		// it should not be more abstract (but the process of counting should be described as process later) 
		Neuron countNeuron = new Neuron(COUNT) {	
			@Override
			public Data compute(StringData operatorName, Data data) {
				return new IntegerData(new TextData(data.toStringData()).elements().length);
			}
		};
		
		Neuron containsNeuron = new Neuron(CONTAINS) {			
			@Override
			public Data compute2(StringData operatorName, Data input1, Data input2) {
				return new BoolData(input1.contains(input2));
			}
		};		
		
		final Neuron getBeginNeuron = new Neuron(GET_FROM_BEGIN) {
			@Override
			public Data compute2(StringData operatorName, Data input1, Data input2) {
				Data[] elements = input1.elements();
				
				try {
					int length = Integer.parseInt(input2.toString());
					ArrayList<Data> result = ArrayData.subdata(Arrays.asList(elements), 0, length);		
					
					if(result != null) {
						return new ArrayData(result);
					} 				
				} catch(Throwable th) {
					// input is not valid
					getLogger().error("second argument is not integer number");
					return null;
				}
				
				return null;
			}
		};
		
		final Neuron getEndNeuron = new Neuron(GET_FROM_END) {
			@Override
			public Data compute2(StringData operatorName, Data input1, Data input2) {
				Data[] elements = input1.elements();
				
				try {
					int length = Integer.parseInt(input2.toString());
					ArrayList<Data> result = ArrayData.subdata(Arrays.asList(elements), elements.length - length, elements.length);		
					
					if(result != null) {
						return new ArrayData(result);
					} 
				} catch(Throwable th) {
					// input is not valid
					getLogger().error("second argument is not integer number");
					return null;
				}
				return null;
			}
		};
		
		Neuron indexOfNeuron = new Neuron(INDEX_OF) {			
			@Override
			public Data compute2(StringData operatorName, Data input1, Data input2) {
				int index = input1.toString().indexOf(input2.toString());
				
				if(index == -1) {
					return null;
				}
				
				return new IntegerData(index + 1);
			}
			
			@Override
			public Neuron bijectionNeuron() {
				//return getBeginNeuron;
				return null;
			}
		};
		
		Neuron backIndexOfNeuron = new Neuron(BACK_INDEX_OF) {			
			@Override
			public Data compute2(StringData operatorName, Data input1, Data input2) {
				String inStr = input1.toString();
				String outStr = input2.toString();
				int lastIndexOf = inStr.lastIndexOf(outStr);
				if(lastIndexOf == -1) {
					return null;
				}
				
				return new IntegerData(inStr.length() - (lastIndexOf + 1) + (outStr.length() - 1) );
			}
			
			@Override
			public Neuron bijectionNeuron() {
				return getEndNeuron;
			}
		};
		
		/**
		 * That neuron need to globalize solution for given text attributes in name
		 * Ex: If trained operator: a * 2 train how to multiply 2. It can automatically learn how to multiply any N.
		 */
		Neuron findOperatorToSolutionParameters = new Neuron(FIND_OPERATOR_TO_SOLUTION_RELATIONS) {
			@Override
			public Set<NeuronData> compute(Neuron neuron) {				
				StringData operatorName = neuron.getName();
				Data[] operPars = ArrayData.splitToElements(operatorName.toString());	
				
				TreeMap<Integer, HashMap<Neuron, Data>> foundRelations = new TreeMap<>();
				TreeMap<Data, TreeSet<Integer> > internalRelations = new TreeMap<>();
				
				int index = 0;
				
				for (Data part: operPars) {
					if(internalRelations.containsKey(part)) {
						internalRelations.get(part).add(index);
					} else {					
						boolean success = false;
						//for(Entry<Neuron, Pair<Data, Double>> neuroRelation: neuron.getRelations().entrySet()) {
						for(RelationsData neuroRelation: neuron.getRelations()) {
							if(part.compareTo(neuroRelation.expectedOutput) == 0) {
								if(!foundRelations.containsKey(index)) {
									foundRelations.put(index, new HashMap<Neuron, Data>());
								}
								
								foundRelations.get(index).put(neuroRelation.processingNeuron, neuroRelation.expectedOutput);
								
								success = true;
							}
						}
						
						if(success) {
							internalRelations.put(part, new TreeSet<Integer>(Arrays.asList(new Integer[]{index})));
						}
					}
					
					++index;
				}
				
				try {
					NeuronData data = new NeuronData(new ParameterisedNeuron(operPars, foundRelations, internalRelations, neuron));
					Set<NeuronData> resultSet = new HashSet<>();
					resultSet.add(data);
					return resultSet;
				} catch (Exception e) {
					getLogger().error("Failed to create parametrised neuron. ");
					return null;
				}
			}
			
			@Override
			public boolean canAnalyseNeurons() {
				return true;
			}
		};
		
		Neuron substring = new Neuron(SUBSTRING_FROM_TO) {
			@Override
			public Data compute(StringData operatorName, ArrayList<Data> data) {
				try {
					int ind1 = new Integer(data.get(1).toString());
					int ind2 = new Integer(data.get(2).toString());
					return new StringData(data.get(0).toString().substring(ind1 + 1, ind2 + 1));
				}
				catch(Throwable t) {
					
				}
				return null;
			}
		};
		
		Neuron substring2 = new Neuron(SUBSTRING_FROM_LEN) {
			@Override
			public Data compute(StringData operatorName, ArrayList<Data> data) {
				try {
					int ind1 = new Integer(data.get(1).toString());
					int ind2 = new Integer(data.get(2).toString());
					return new StringData(data.get(0).toString().substring(ind1 + 1, ind1 + ind2 + 1));
				}
				catch(Throwable t) {
					
				}
				return null;
			}
		};
		
		Neuron backSubstring = new Neuron(BACK_SUBSTRING_FROM_TO) {
			@Override
			public Data compute(StringData operatorName, ArrayList<Data> data) {
				try {
					int ind1 = new Integer(data.get(1).toString());
					int ind2 = new Integer(data.get(2).toString());
					String string = data.get(0).toString();
					return new StringData(string.substring(string.length() - ind2, string.length() - ind1));
				}
				catch(Throwable t) {
					
				}
				return null;
			}
		};
		
		Neuron backSubstring2 = new Neuron(SUBSTRING_FROM_LEN) {
			@Override
			public Data compute(StringData operatorName, ArrayList<Data> data) {
				try {
					int ind1 = new Integer(data.get(1).toString());
					int ind2 = new Integer(data.get(2).toString());
					String string = data.get(0).toString();
					return new StringData(string.substring(string.length() - ind1, string.length() - ind1 + ind2));
				}
				catch(Throwable t) {
					
				}
				return null;
			}
		};
		
		Neuron sameElements = new Neuron(HAS_SAME_ELEMENTS) {
			@Override
			public Data compute(StringData operatorName, ArrayList<Data> data) {
				try {
					if(data.size() < 2) {
						return new BoolData(true);
					} 
					
					Data first = data.get(0);
					
					for(int i = 1; i < data.size(); ++ i) {
						if(first.compareTo(data.get(i)) != 0) {
							return new BoolData(false);
						}
					}
					
					return new BoolData(true);
				}
				catch(Throwable t) {
					
				}
				return null;
			}
		};
		
		Neuron findInvariantRelationsNeuron = new InvariantNeuronAnalysingNeuron();
		
		add(result, countNeuron);
		add(result, containsNeuron);
		add(result, indexOfNeuron);	
		add(result, backIndexOfNeuron);	
		add(result, getBeginNeuron);
		add(result, getEndNeuron);	
		add(result, findOperatorToSolutionParameters);
		add(result, findInvariantRelationsNeuron);
		add(result, substring);
		add(result, substring2);
		add(result, backSubstring);
		add(result, backSubstring2);
		add(result, sameElements);
		
		return result;		
	}

	private static void add(HashMap<Data, Neuron> result, Neuron countNeuron) {
		result.put(countNeuron.getName(), countNeuron);
	}
}
