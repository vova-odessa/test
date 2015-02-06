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
import nntest2.data.ComputationStatistic.DataPredicate;
import nntest2.data.ComputationStatistic.RelationsData;
import nntest2.data.Data;
import nntest2.data.IntegerData;
import nntest2.data.InvokationData;
import nntest2.data.NeuronData;
import nntest2.data.StringData;
import nntest2.data.TextData;
import nntest2.neurons.GroupAnalysisNeuron;
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
	
	public static final GroupAnalysisNeuron groupAnalysis = new GroupAnalysisNeuron();
	
	public static final Neuron countNeuron = new Neuron(COUNT) {	
		@Override
		public Data compute(NeuronData operatorName, Data data) {
			return new IntegerData(data.elements().length);
		}
	};
	public static final Neuron containsNeuron = new Neuron(CONTAINS) {			
		@Override
		public Data compute2(NeuronData operatorName, Data input1, Data input2) {				
			if(input2 == null || input1 == null || input2.toString().length() == 0) {
				return null;
			}
			return new BoolData(input1.contains(input2));
		}
	};
	public static final Neuron getBeginNeuron = new Neuron(GET_FROM_BEGIN) {
		@Override
		public Data compute2(NeuronData operatorName, Data input1, Data input2) {
			if(input1 == null || input2 == null) {
				return null;
			}
			
			Data[] elements = input1.elements();
			
			try {
				int length = Integer.parseInt(input2.toString());
				
				if(length <= 0) {
					return null;
				}
				
				ArrayList<Data> result = ArrayData.subdata(Arrays.asList(elements), 0, length);		
				
				if(result != null) {
					return new ArrayData(result);
				} 				
			} catch(Throwable th) {
				// input is not valid
				//getLogger().error("second argument is not integer number");
				return null;
			}
			
			return null;
		}
	};
	private static final Neuron getEndNeuron = new Neuron(GET_FROM_END) {
		@Override
		public Data compute2(NeuronData operatorName, Data input1, Data input2) {
			if(input1 == null || input2 == null) {
				return null;
			}
			
			Data[] elements = input1.elements();
			if(elements.length < 2) {
				elements = input1.forceElements();
			}
			
			try {
				int length = Integer.parseInt(input2.toString());
				
				if(length < 0) {
					return null;
				}
				
				ArrayList<Data> result = ArrayData.subdata(Arrays.asList(elements), elements.length - length, elements.length);		
				
				if(result != null) {
					//return new Text(result);
					Data[] arr = new Data[result.size()];
					return new StringData(result.toArray(arr));
				} 
			} catch(Throwable th) {
				// input is not valid
				//getLogger().error("second argument is not integer number");
				return null;
			}
			return null;
		}
	};
	public static final Neuron indexOfNeuron = new Neuron(INDEX_OF) {			
		@Override
		public Data compute2(NeuronData operatorName, Data input1, Data input2) {
			if(input1 == null || input2 == null || input2.toString().length() == 0) {
				return null;
			}
			
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
	public static final Neuron backIndexOfNeuron = new Neuron(BACK_INDEX_OF) {			
		@Override
		public Data compute2(NeuronData operatorName, Data input1, Data input2) {
			if(input1 == null || input2 == null || input2.toString().length() == 0) {
				return null;
			}
			
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
	public static final Neuron findOperatorToSolutionParameters = new Neuron(FIND_OPERATOR_TO_SOLUTION_RELATIONS) {
		@Override
		public Set<NeuronData> compute(Neuron neuron) {		
			if(neuron == null) {
				return null;
			}
			
			StringData operatorName = neuron.getName();
			Data[] operPars = //ArrayData.splitToElements(operatorName.toString());//Data.construct(operatorName.toString()).elements();
					new TextData(operatorName.toString().split(" ")).elements();
			
			if(operPars == null) {
				operPars = new Data[]{operatorName};
			}
			
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
						if(neuroRelation.expectedOutput != null && part.compareTo(neuroRelation.expectedOutput) == 0) {
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
	public static final Neuron substring = new Neuron(SUBSTRING_FROM_TO) {
		@Override
		public Data compute(NeuronData operatorName, ArrayList<Data> data) {
			if(data == null) {
				return null;
			}
			try {
				int ind1 = new Integer(data.get(1).toString());
				int ind2 = new Integer(data.get(2).toString());
				
				if(ind1 >= ind2 || ind2 <= 0) {
					return null;
				}
				
				return new StringData(data.get(0).toString().substring(ind1 + 1, ind2 + 1));
			}
			catch(Throwable t) {
				
			}
			return null;
		}
	};
	public static final Neuron substring2 = new Neuron(SUBSTRING_FROM_LEN) {
		@Override
		public Data compute(NeuronData operatorName, ArrayList<Data> data) {
			try {
				int ind1 = new Integer(data.get(1).toString());
				int len = new Integer(data.get(2).toString());
				
				if(ind1 < 0) {
					return null;
				}
				
				return new StringData(data.get(0).toString().substring(ind1 + 1, ind1 + len + 1));
			}
			catch(Throwable t) {
				
			}
			return null;
		}
	};
	public static final Neuron backSubstring = new Neuron(BACK_SUBSTRING_FROM_TO) {
		@Override
		public Data compute(NeuronData operatorName, ArrayList<Data> data) {
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
	public static final Neuron backSubstring2 = new Neuron(SUBSTRING_FROM_LEN) {
		@Override
		public Data compute(NeuronData operatorName, ArrayList<Data> data) {
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
	public static final Neuron sameElements = new Neuron(HAS_SAME_ELEMENTS) {
		@Override
		public Data compute(NeuronData operatorName, ArrayList<Data> data) {
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
	public static final Neuron findInvariantRelationsNeuron = new InvariantNeuronAnalysingNeuron();
	public static final Neuron train = new Neuron("train") {
		public Data compute(NeuronData operatorName, ArrayList<Data> input) {
			if(operatorName != null && operatorName.isValid() && operatorName.getNeuron().compareTo(equal) == 0) {
				if(input.size() == 2) {				
					InvokationData invocation = generateSimpleInvocation(Data.construct(input.get(0).toString()).elements());
					
					if(invocation == null || !invocation.neuron.isValid()) {
						return null;
					}
					
					invocation.neuron.getNeuron().train(operatorName, invocation.inputMapping, Data.construct(input.get(1).toString()));
					
					getLogger().info(invocation.neuron.getNeuron().toString());
				} else {
					//
				}
			}
			
			return null;
		}
		
		public boolean canBeUsedForGeneralComputation() {
			return false;
		};
	};
	
	public static final Neuron compute = new Neuron("compute") {
		public Data compute(NeuronData operator, Data input) {
			return compute(operator, new ArrayList<Data>(Arrays.asList(input.elements())));
		}
		
		public Data compute2(NeuronData operator, Data input1, Data input2) {
			return compute(operator, new ArrayList<Data>(Arrays.asList(new Data[]{input1, input2})));
		}
		
		public Data compute(NeuronData operatorName, ArrayList<Data> input) {
			if(operatorName != null && operatorName.isValid() && operatorName.getNeuron().compareTo(equal) == 0) {
				return equal.compute(operatorName, input);
			} else if(operatorName == null || !operatorName.isValid() || operatorName.getNeuron().compareTo(this) == 0) {
				// try to find top level operators
				
				Data[] splited = new ArrayData(input).splitBy(new NeuronData(equal));
				
				if(splited != null) {
					return compute(equal.toData(), new ArrayList<Data>(Arrays.asList(splited)));
				}
				
				// XXX check other operators like =>, ...
				
				// just compute
				return compute(equal.toData(), new ArrayList<Data>(Arrays.asList(new Data[]{new ArrayData(input)})));
			}
			
			return null;
		}
		
		public boolean canBeUsedForGeneralComputation() {
			return false;
		}
	};
	
	public static final Neuron equal = new Neuron("=") {
		public Data compute2(NeuronData operatorName, Data input1, Data input2) {
			Data result1 = compute(operatorName, input1);	
			Data result2 = compute(operatorName, input2);
			
			if( result1 == null || result2 == null || result1.compareTo(result2) != 0 ) {
				return train.compute(operatorName, CommonHelper.mergeCopy(input1, input2));
			} else {
				return result1;
			}
		}
		
		public Data compute(NeuronData operatorName, Data input) {
			if(input == null) {
				return null;
			}
			
			input = Data.construct(input.toString());
			
			if(input instanceof NeuronData) {
				return ((NeuronData) input).getNeuron().compute(toData(), new ArrayList<Data>());
			} else {
				Data[] elements = input.elements();			
				if(elements.length == 1) {
					if(elements[0].compareTo(input) == 0) {
						return elements[0];
					}
					// call recursively
					return compute(operatorName, elements[0]);
				}
				
				InvokationData data = generateSimpleInvocation(elements);
				
				if(data == null) {
					return input;
				}
				
				return data.invoke(null, null, null);
			}
		}
		
		
	};
	
	public static InvokationData generateSimpleInvocation(Data[] elements) {
		ArrayList<Integer> operatorIndexes = new ArrayList<>();
		ArrayList<Data> inputData = new ArrayList<>();
		
		int index = 0;
		for (Data data : elements) {
			if(data instanceof NeuronData && ((NeuronData) data).isValid()) {
				operatorIndexes.add(index);
				if(operatorIndexes.size() > 1) {
					inputData.add(data);
				}
			} else {
				inputData.add(data);
			}
				
			++ index;
		}
		
		if(operatorIndexes.size() == 0) {
			return null;
		}
		
		InvokationData data = null;
		try {
			data = new InvokationData(((NeuronData)elements[operatorIndexes.get(0)]), inputData);
		} catch (Exception e) {
			return null;
		}
		
		return data;
	}

	public static HashMap<Data, Neuron> createBaseNeurons(NeuroBase base) {
		HashMap<Data, Neuron> result = new HashMap<>();
		
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
		add(result, groupAnalysis);
		add(result, compute);
		add(result, equal);
		add(result, train);
		
		return result;		
	}

	private static void add(HashMap<Data, Neuron> result, Neuron countNeuron) {
		result.put(countNeuron.getName(), countNeuron);
	}
}
