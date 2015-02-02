package nntest2.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.sec.gb.ipa.ks.common.data.Pair;
import com.sec.gb.ipa.ks.common.util.Common;

import nntest2.data.ComputationStatistic.ResultExperience;
import nntest2.neurons.Neuron;

public class ComputationStatistic {
	public static class DataPredicate {
		public Data data;
		public Boolean used;
		public int index = -1;
		
		public DataPredicate(Data data, Boolean used, int index) {
			this.data = data;
			this.used = used;
		}
		
		@Override
		public String toString() {
			if(used) {
				if(index == -1) {
					return data.toString();
				} else {
					return new IndexData(index).toString();
				}
			} else {
				return "";
			}
		}
		
		public static ArrayList<Data> extract(ArrayList<DataPredicate> predicatedInput) {
			ArrayList<Data> result = new ArrayList<>();
			
			if(predicatedInput != null) {
				for (DataPredicate data : predicatedInput) {
					if(data.used) {
						result.add(data.data);
					}
				}
			}
			return result;			
		}
		
		public static ArrayList<Data> extract(ArrayList<DataPredicate> predicatedInput, ArrayList<Data> parentInput) {
			ArrayList<Data> result = new ArrayList<>();
			
			if(predicatedInput != null) {
				for (DataPredicate data : predicatedInput) {
					if(data.used) {
						//result.add(data.data);
						if(data.index == -1) {
							result.add(data.data);
						} else {
							result.add(parentInput.get(data.index));
						}
					}
				}
			}
			return result;			
		}
		
		public static ArrayList<DataPredicate> cover(ArrayList<Data> predicatedInput) {
			ArrayList<DataPredicate> result = new ArrayList<>();
			
			if(predicatedInput != null) {
				int index = 0;
				for (Data data : predicatedInput) {
					result.add(new DataPredicate(data, true, index ++));
				}
			}
			return result;			
		}
	}
	
	public static class ResultExperience {
		public Neuron neuron;
		public ArrayList<DataPredicate> input;
		public Data output;
		public Data parentOutput;
		public ArrayList<Data> inputConfiguration;
		
		public ResultExperience(Neuron neuron, ArrayList<DataPredicate> input, Data output, Data parentOutput) {
			this.neuron = neuron;
			this.input = input;
			this.output = output;
			this.parentOutput = parentOutput;
			
			inputConfiguration = new ArrayList<>();
			for (DataPredicate predicate : input) {
				//inputConfiguration.add(predicate.used);
				inputConfiguration.add(new IndexData(predicate.index));
			}
		}
		
		@Override
		public String toString() {
			String tab = "";			
			return toString(tab);
		}

		private String toString(String tab) {
			StringBuilder resultBuilder = new StringBuilder();
			resultBuilder.append(tab + "(#experience data=\n");
			
			if(neuron != null) {
				resultBuilder.append(tab + "\tneuron name: " + neuron.getName() + "\n");
			}
			
			if(input != null && !input.isEmpty()) {
				String result = input.toString();
				
				if(result.length() > 2 + input.size()) {
					// means it have not empty elements
					resultBuilder.append(tab + "\tinput: " + result + "\n");
				}
			}
			
			if(output != null) {
				resultBuilder.append(tab + "\toutput: " + output.toString() + "\n");
			}
			
			if(parentOutput != null) {
				resultBuilder.append(tab + "\tparentOutput: " + parentOutput.toString() + "\n");
			}
			
			return resultBuilder.append(tab + ")\n").toString();
		}
 	}
	
	public class ExperienceData implements Comparable<ExperienceData>{
		public Data time;
		public HashMap<Neuron, ResultExperience> computationResults;
		
		public ExperienceData(Data time, HashMap<Neuron, ResultExperience> computationResults) {
			this.time = time;
			this.computationResults = computationResults;
		}
		
		public ExperienceData(Data time, Collection<ResultExperience> computationResults) {
			this.time = time;
			this.computationResults = new HashMap<>();
			for (ResultExperience result : computationResults) {
				this.computationResults.put(result.neuron, result);
			}
		}
		
		@Override
		public String toString() {
			String tab = "";			
			return toString(tab);
		}

		private String toString(String tab) {
			StringBuilder resultBuilder = new StringBuilder();
			resultBuilder.append(tab + "(#experience data=\n");
			
			if(time != null) {
				resultBuilder.append(tab + "\ttime: " + time.toString() + "\n");
			}
			
			if(computationResults != null && computationResults.size() > 0) {
				resultBuilder.append(tab + "\t[\n");
				for (ResultExperience value : computationResults.values()) {
					resultBuilder.append(value.toString(tab + "\t\t") + ",\n");
				}
				resultBuilder.append(tab + "\t]\n");
			}
			
			return resultBuilder.append(tab + ")\n").toString();
		}

		@Override
		public int compareTo(ExperienceData other) {
			int timeCompare = time.toString().compareTo(other.time.toString());
			if(timeCompare != 0) {
				return timeCompare;
			}
			
			return Integer.compare(this.hashCode(), other.hashCode());
		}
		
		
	}
	
	public class ExperienceSet {
		public TreeMap<Data, TreeSet<ExperienceData> > experienceByTime = new TreeMap<Data, TreeSet<ExperienceData>>();
		public TreeMap<Data, TreeSet<ExperienceData> > experienceByOutput = new TreeMap<Data, TreeSet<ExperienceData>>();
		public HashMap<ArrayList<Data>, TreeSet<ExperienceData> > experienceByInput= new HashMap();
		public TreeSet<ExperienceData> allExperience= new TreeSet();
		
		@Override
		public String toString() {
			String tab = "";			
			return toString(tab);
		}

		private String toString(String tab) {
			StringBuilder resultBuilder = new StringBuilder();
			resultBuilder.append(tab + "(#statistic=\n");
			
			for (Entry<Data, TreeSet<ExperienceData>> entry : experienceByTime.entrySet()) {
				for (ExperienceData data : entry.getValue()) {
					resultBuilder.append(data.toString(tab));
				}
			}
			
			return resultBuilder.append(tab + ")\n").toString();
		}
		
		public void addData(ArrayList<Data> input, Data output, Data time, ArrayList<ResultExperience> context) {			
			ExperienceData data = new ExperienceData(time, context);
			validate(experienceByTime, time).add(data);
			validate(experienceByInput, input).add(data);
			validate(experienceByOutput, output).add(data);
			allExperience.add(data);
		}
		
		public <T>TreeSet<ExperienceData> validate(Map<T, TreeSet<ExperienceData> > set, T data) {
			if(!set.containsKey(data)) {
				set.put(data, new TreeSet<ExperienceData>());
			}
			
			return set.get(data);
		}
	}
	
	public static class RelationsData {
		public Neuron relationNeuron;
		public ArrayList<DataPredicate> inputConfiguration;
		public Neuron processingNeuron;
		public Data expectedOutput;
		//public Data actualOutput;
		public Boolean expectedUsedAsLastInput = false;
		public InvokationData invocation = null;
		
		@Override
		public String toString() {
			String tab = "";
			return toString(tab);
		}

		private String toString(String tab) {
			StringBuilder resultBuilder = new StringBuilder();
			resultBuilder.append(tab + "(#relation statistic=\n");
			
			if(relationNeuron != null) {
				resultBuilder.append(tab + "\trelation type: " + relationNeuron.getName() + ",\n");
			}
			
			if(processingNeuron != null) {
				resultBuilder.append(tab + "\toperator: " + processingNeuron.getName() + ",\n");
			}
			
			if(inputConfiguration != null) {
				resultBuilder.append(tab + "\tinput: " + inputConfiguration.toString() + ",\n");
			}
			
			/*if(actualOutput != null) {
				resultBuilder.append(tab + "\toutput: " + actualOutput.toString() + ",");
				if(expectedUsedAsLastInput) {
					resultBuilder.append("+(used expected output as input) " + expectedOutput.toString() + ",\n");
				} else {
					resultBuilder.append("\n");
				}
			}*/
			
			if(invocation != null) {
				resultBuilder.append(tab + "\tinvocation: " + invocation.toString() + ",\n");
			}
			
			if(!expectedUsedAsLastInput && expectedOutput != null) {
				resultBuilder.append(tab + "\texpected output: " + expectedOutput.toString() + ",\n");
			}
			
			return resultBuilder.append(tab + ")\n").toString();
		}
	}
	
	/*******************************************************************************************
	 * Actual class data
	 */
	
	/** 
	 * time sorted experience data
	 */
	public Map<ArrayList<Data>, ExperienceSet> experienceByInput = new HashMap();
	public ExperienceSet globalExperience = new ExperienceSet();
	
	/**
	 * relations of all types found
	 */
	public Set<RelationsData> relations = null;
	
	@Override
	public String toString() {
		StringBuilder resultBuilder = new StringBuilder();
		resultBuilder.append("(#computation statistic=\n");
		
		if(globalExperience != null) {			
			resultBuilder.append(globalExperience.toString("\t"));
		}
		
		if(relations != null) {
			//resultBuilder.append(relations.toString("\t") + ",\n");
			for (RelationsData relation : relations) {
				resultBuilder.append(relation.toString("\t"));
			}
		}
		
		return resultBuilder.append(")").toString();
	}
	
	private ExperienceSet validateInputExperience(ArrayList<Data> input) {
		if( experienceByInput.containsKey(input) ) {
			return experienceByInput.get(input);
		}
		
		ExperienceSet newData = new ExperienceSet();
		experienceByInput.put(input, newData);
		
		return newData;
	}
	
	public ExperienceSet getInputExperience(ArrayList<Data> input) {
		return experienceByInput.get(input);
	}
	
	public void addExperience(ArrayList<Data> input, Data output, Data time, ArrayList<ResultExperience> context) {
		// add to input-related storage
		ExperienceSet inputRelatedStorage = validateInputExperience(input);
		inputRelatedStorage.addData(input, output, time, context);
		
		// add to global storage
		globalExperience.addData(input, output, time, context);
	}
	
	public TreeSet<ExperienceData> getAllExperience() {
		return globalExperience.allExperience;
	}
	
	public HashMap<Neuron, HashMap<Data, Pair<ResultExperience, Double>>> computeSummary() {
		HashMap<Neuron, HashMap<Data, Pair<ResultExperience, Double>>> summary = new HashMap<>();
		
		// compute values distribution
			
		for(ExperienceData values: getAllExperience()) {
			if(values != null) {				
				Map<Neuron, ResultExperience> vals = values.computationResults;
				for(Entry<Neuron, ResultExperience> val: vals.entrySet()) {
					if(!summary.containsKey(val.getKey())) {
						summary.put(val.getKey(), new HashMap<Data, Pair<ResultExperience, Double>>());
					}
					HashMap<Data, Pair<ResultExperience, Double>> stats = summary.get(val.getKey());
					if(!stats.containsKey(val.getValue().output)) {
						stats.put(val.getValue().output, new Pair<ResultExperience, Double>(val.getValue(), 1.0));
					} else {
						Pair<ResultExperience, Double> stat = stats.get(val.getValue().output);
						stat.second += 1.0;
					}
				}		
			}
		}
		
		return summary;
	}
	
	public HashMap<Neuron, ArrayList<Data>> computeValues() {
		HashMap<Neuron, ArrayList<Data>> summary = new HashMap<>();
		
		// compute values distribution
			
		for(ExperienceData values: getAllExperience()) {
			if(values != null) {				
				Map<Neuron, ResultExperience> vals = values.computationResults;
				for(Entry<Neuron, ResultExperience> val: vals.entrySet()) {
					if(!summary.containsKey(val.getKey())) {
						summary.put(val.getKey(), new ArrayList<Data>());
					}
					
					summary.get(val.getKey()).add(val.getValue().output);
				}		
			}
		}
		
		return summary;
	}
	
	public HashMap<Neuron, ArrayList<Data>> computeExpectedOutputs() {
		HashMap<Neuron, ArrayList<Data>> summary = new HashMap<>();
		
		// compute values distribution
			
		for(ExperienceData values: getAllExperience()) {
			if(values != null) {				
				Map<Neuron, ResultExperience> vals = values.computationResults;
				for(Entry<Neuron, ResultExperience> val: vals.entrySet()) {
					if(!summary.containsKey(val.getKey())) {
						summary.put(val.getKey(), new ArrayList<Data>());
					}
					
					summary.get(val.getKey()).add(val.getValue().parentOutput);
				}		
			}
		}
		
		return summary;
	}
	
	public static HashMap<Neuron, HashMap<Data, Pair<ResultExperience, Double>>> computeDistribution(HashMap<Neuron, HashMap<Data, Pair<ResultExperience, Double>>> summary) {
		HashMap<Neuron, HashMap<Data, Pair<ResultExperience, Double>>> distribution = new HashMap<>();
		
		for(Entry<Neuron, HashMap<Data, Pair<ResultExperience, Double>>> answerStats: summary.entrySet() ) {
			Double sum = 0.0;			
			// compute
			for(Pair<ResultExperience, Double> appearences: answerStats.getValue().values()) {
				sum += appearences.second;
			}
			
			// normalize
			for( Entry<Data, Pair<ResultExperience, Double>> appearences: answerStats.getValue().entrySet() ) {
				//appearences.setValue(appearences.getValue()/sum);
				if(!distribution.containsKey(answerStats.getKey())) {
					distribution.put(answerStats.getKey(), new HashMap<Data, Pair<ResultExperience, Double>>());
				}
				
				Pair<ResultExperience, Double> value = appearences.getValue();
				distribution.get(answerStats.getKey()).put(appearences.getKey(), new Pair<>(value.first, value.second / sum));
			}
		}
		
		return distribution;
	}
	
	public boolean validateRelations( Set<RelationsData> newRelations ) {
		boolean areEqual = Common.areEqual(relations, newRelations);
		
		if(areEqual) {
			return false;
		}
		
		relations = newRelations;
		return true;
	}
	
	public boolean knowRelations() {
		return relations != null && !relations.isEmpty();
	}
}
