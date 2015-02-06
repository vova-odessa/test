package nntest2.neurons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import nntest2.Analyser;
import nntest2.NeuroBase;
import nntest2.data.AnalysisRequest;
import nntest2.data.ArrayData;
import nntest2.data.BoolData;
import nntest2.data.ComputationStatistic;
import nntest2.data.ComputationStatistic.DataPredicate;
import nntest2.data.ComputationStatistic.ExperienceData;
import nntest2.data.ComputationStatistic.ExperienceSet;
import nntest2.data.ComputationStatistic.RelationsData;
import nntest2.data.ComputationStatistic.ResultExperience;
import nntest2.data.Data;
import nntest2.data.IndexData;
import nntest2.data.IntegerData;
import nntest2.data.InvokationData;
import nntest2.data.NeuronData;
import nntest2.data.StringData;
import nntest2.data.TextData;
import nntest2.herpers.CommonHelper;
import nntest2.herpers.NeuroHelper;

import org.apache.log4j.Logger;

import com.sec.gb.ipa.ks.common.data.Pair;
import com.sec.gb.ipa.ks.common.util.Common;

/**
 * In this conception handle + compute + activation can happen on same step.
 * This made to provide continuous system with out central control.
 * 
 * 1 Neuron analyzes 1 static heuristic.
 * If heuristic not works, neuron become dead. 
 * (But not removed ???, to avoid it future examination. Seems depend on target goal, can be both.)
 * 
 * @author sec
 */
public class Neuron implements Comparable<Neuron> {
	private static final int SIMILARITY_LEARN_RATE = 3;

	private Logger logger = null;
	
	private StringData name = null; 
	private static final double LEARN_TRESHOLD = 0.2;
	private static final double INVARIANT_LEARN_ACCEPT_TRESHOLD = 0.99;
	
	/**
	 * 1st Data - the input value
	 * 2nd Data - the result value
	 * 3rd Data - the time value
	 * Neuron   - the neuron which state significant for context
	 * 4th Data - the context neuron state 
	 * 
	 */
	//private HashMap<ArrayList<Data>, HashMap<Data, TreeMap<Data, HashMap<Neuron, Data> >> > knowledge = new HashMap<>();
	//private HashMap<Neuron, Pair<Data, Double> > acceptedConfiguration = new HashMap<>();
	private HashSet<Neuron> kidNeurons = new HashSet<>();
	private ComputationStatistic knowledge = new ComputationStatistic();	
	private HashMap<Neuron, HashSet<InvokationData>> relations = new HashMap<>();
	
	public void addRelations(Neuron relation, Collection<InvokationData> objects) {
		if(!relations.containsKey(relation)) {
			relations.put(relation, new HashSet<>(objects));
		} else {
			relations.get(relation).addAll(objects);
		}
	}
	
	public void addRelations(Neuron relation, InvokationData object) {
		addRelations(relation, Arrays.asList(new InvokationData[]{object}));
	}
	
	public HashSet<InvokationData> getRelations(Neuron relation) {
		return relations.get(relation);
	}
	
	public void destroyRelations() {
		relations = new HashMap<>();
	}
	
	public boolean removeRelation(Neuron relation, Neuron relationObject) {
		if(!relations.containsKey(relation)) {
			return false;
		}
		
		HashSet<InvokationData> badRelations = new HashSet<>();
		HashSet<InvokationData> rels = relations.get(relation);
		for (InvokationData rel : rels) {
			if(rel.neuron.getNeuron().compareTo(relationObject) == 0) {
				badRelations.add(rel);
			}
		}
		
		rels.removeAll(badRelations);
		return badRelations.size() > 0;
	}
	
	public void addKid(Neuron kid) {
		kidNeurons.add(kid.register());
	}
	
	public void destroy() {
		destroyKids();
		NeuroBase.getInstance().destroy(this);
	}

	private void destroyKids() {
		for (Neuron kid : kidNeurons) {
			kid.destroy();
		}
	}
	
	public Neuron register() {
		return NeuroBase.getInstance().register(this);
	}
	
	public boolean validate(String name) {
		return false;
	}
	
	/**
	 * Means neuron can depend from variables and name can be validated to requested operator
	 * @return
	 */
	public boolean canValidate() {
		return false;
	}
	
	public boolean canAnalyseText() {
		return false;
	}
	
	public boolean canBeUsedForGeneralComputation() {
		return true;
	}
	
	public boolean isVerificationRelation() {
		return false;
	}
	
	public boolean isComputationRelation() {
		return false;
	}
	
	/**
	 * That means neuron can analyse the input neurons to find kids that are depends from variable.
	 * @return
	 */
	public boolean canAnalyseNeurons() {
		return false;
	}
	
	/**
	 * Means neuron can process correct results of other neurons in order to find Neuron -> Neuron extended relations
	 * @return
	 */
	public boolean canAnalyseResults() {
		return false;
	}
	
	/**
	 * 
	 * @param text
	 * @return
	 * 	- InvocationData - if text defines the general method, that can be computed with some attributes
	 *  - null - if there was just information, and it not need to be return anything
	 *  - other Data - the result of analysis
	 */
	public Data analyseText(TextData text) {
		return null;		
	}
	
	public Set<NeuronData> compute(Neuron inputNeuron) {
		return null;
	}
	
	public void compute(Neuron inputNeuron, NeuronData operator, ArrayList<Data> input, Data output) {
		compute(new AnalysisRequest(inputNeuron, operator, input, output));
	}
	
	public void compute(AnalysisRequest request) {
		
	}
	
	public Data compute(NeuronData operator, Data input) {	
		return findAnswer(operator, CommonHelper.mergeCopy(input));
	}

	private Data findAnswer(NeuronData operator, ArrayList<Data> input) {
		Set<Data> prevAnswers = getKnowledge(input, null);
		
		if(prevAnswers.size() == 1) {
			// have only 1 unambiguous answer
			return prevAnswers.iterator().next();
		}
		
		return computeEx(operator, input);
	}
	
	public Set<RelationsData> getRelations() {
		return knowledge.relations;
	}
	
	public Data computeEx(NeuronData operator, ArrayList<Data> input) {
		return computeEx(operator, input, null);
	}

	/**
	 * This method computes using logic (no memory data)
	 * @param input 
	 * @return
	 */
	public Data computeEx(NeuronData operator, ArrayList<Data> input, HashMap<Neuron, Data> alternativeData) {
		// try to find answer
		
		Set<Data> returnedData = new HashSet<>();
		
		Set<Data> results = new HashSet<>();
		
		for(Entry<Neuron, HashSet<InvokationData>> relation : relations.entrySet()) {
			
			for (InvokationData solution : relation.getValue()) {
				if(relation.getKey().isComputationRelation()) {
					Data result = null; 
					Data alternative = null;
					
					if(alternativeData != null) {
						if(alternativeData.containsKey(solution.neuron.getNeuron())) {
							alternative = alternativeData.get(solution.neuron.getNeuron());
						} else if(alternativeData.containsKey(solution.subjectNeuron.getNeuron())) {
							alternative = alternativeData.get(solution.subjectNeuron.getNeuron());
						}
					}
					
					InvokationData foundSolution = solution.clone();
					
					if(alternative != null) {
						foundSolution.inputMapping = ParameterisedNeuron.replaceData(foundSolution.inputMapping, alternative);
					}
					
					result = foundSolution.invoke(input, null, null);
					
					if(result != null ) {
						results.add(result);
					}
				}				
			}
			
		}
		
		if(results.size() != 1) {
			return null;
		}
		
		Data result = results.iterator().next();
		// apply global verifications after computation. That should include the only found result
		
		for(Entry<Neuron, HashSet<InvokationData>> relation : relations.entrySet()) {			
			for (InvokationData solution : relation.getValue()) {
				
				if(relation.getKey().isVerificationRelation()) {
					if(!solution.validate(input, result, null)) {
						// input not compatible
						return null;
					}
				}
			}
		}
				
		return result;
	}
	
	public Data compute(NeuronData operator, Data input, Data output) {
		Data result = compute(operator, input);
		return new BoolData( result != null && result.compareTo(output) == 0 );
	}
	
	public Data compute2(NeuronData operator, Data input1, Data input2) {
		return findAnswer(operator, CommonHelper.mergeCopy(input1, input2));
	}
	
	public Data compute2(NeuronData operator, Data input1, Data input2, Data output) {
		Data result = compute2(operator, input1, input2);
		return new BoolData( result != null && result.compareTo(output) == 0 );
	}
	
	public Neuron bijectionNeuron() {
		return null;
	}

	public Data compute(NeuronData operator, ArrayList<Data> input) {
		Data result = null;
		if(input.size() == 1) {
			result = compute(operator, input.get(0));
		} else if(input.size() == 2) {
			result = compute2(operator, input.get(0), input.get(1));
		} else {
			Data answer = findAnswer(operator, input);
			
			if(answer != null) {
				result = answer;
			} else {			
				result = computeEx(operator, input);
			}
		}
		
		if(result != null) {
			// make analysis of computed results
			NeuroBase.getInstance().analyseExperience(this, operator, input, result);
		}
		
		return result;
	}
	
	public Data compute(NeuronData operator, ArrayList<Data> input, Data output) {
		Data result = compute(operator, input);
		if(result == null) {
			return null;
		}
		return new BoolData(result.compareTo(output) == 0);
	}
	
	public Neuron(String name) {
		this.name = new StringData(name);
		logger = Logger.getLogger("Neuron : " + name);
	}
	
	protected void setName(String name) {
		this.name = new StringData(name);
	}
	
	public StringData getName() {
		return name;
	}
	
	public void setKnowledge(ArrayList<Data> input, Data output, Data time, ArrayList<ResultExperience> context) {
		knowledge.addExperience(input, output, time, context);
	}
	
	/**
	 * 
	 * @param input
	 * @param time [optional] the time when 
	 * @param context [optional] the context where
	 * @return
	 */
	public Set<Data> getKnowledge(ArrayList<Data> input, HashMap<Neuron, Data> context) {
		Set<Data> foundData = new HashSet<>();
		//HashMap<Data, TreeMap<Data, HashMap<Neuron, Data> >> values = knowledge.get(input);
		ExperienceSet values = knowledge.getInputExperience(input);
		
		if(values != null) {			
			//for (Entry<Data, TreeMap<Data, HashMap<Neuron, Data>>> results : values.entrySet()) {
			for (Entry<Data, TreeSet<ExperienceData>> results : values.experienceByOutput.entrySet()) {
				//TreeMap<Data, HashMap<Neuron, Data>> contextData = results.getValue();
				
				//for( Entry<Data, HashMap<Neuron, Data>> contectEntry: contextData.entrySet() ) {					
				for( ExperienceData contectEntry: results.getValue() ) {				
					boolean contextCompatible = true;
					
					/*if(context != null) {
						for (Entry<Neuron, Data> contextElement : context.entrySet()) {
							HashMap<Neuron, Data> pastContext = contectEntry.getValue();
							if(!pastContext.containsKey(contextElement.getKey()) 
									|| !(pastContext.get(contextElement.getKey()).compareTo(contextElement.getValue()) == 0)) {
								contextCompatible = false;
							}
						}
					}*/
					// TODO rewrite context validation in future. It will be need for context aware computations
					
					if(contextCompatible) {
						foundData.add(results.getKey());
					}
				}
			}
		}
		
		return foundData;
	}
	
	private void trainRelations(ArrayList<Data> input, Data output) {		
		HashMap<Neuron, HashMap<Data, Pair<ResultExperience, Double>>> summary = knowledge.computeSummary();
		HashMap<Neuron, HashMap<Data, Pair<ResultExperience, Double>>> distribution = ComputationStatistic.computeDistribution(summary);
		HashMap<Neuron, ArrayList<Data>> vals = knowledge.computeValues();
		HashMap<Neuron, ArrayList<Data>> outputs = knowledge.computeExpectedOutputs();
		
		////////////////////////////////////////////
		// compute invariant statistic 
		// ??? just suggest relation neurons candidates? that should be verified and connected to actual relation type?
		HashSet<RelationsData> relations = new HashSet();
		// normalize statistic
		NeuroBase baseInstance = NeuroBase.getInstance();
		for(Entry<Neuron, HashMap<Data, Pair<ResultExperience, Double>>> answerStats: distribution.entrySet() ) {
		
			///////////////////////////////////////////////////////
			// 1. inspect neuron outputs for stability
			Neuron neuron = answerStats.getKey();
			
			for( Entry<Data, Pair<ResultExperience, Double>> appearences: answerStats.getValue().entrySet() ) {
				if(appearences.getValue().second > INVARIANT_LEARN_ACCEPT_TRESHOLD) {
					if(appearences.getKey() != null) {
						//invariants.add(appearences.getKey());
						RelationsData relation = new RelationsData();
						relation.processingNeuron = neuron;
						relation.expectedUsedAsLastInput = appearences.getValue().first.expectedUsedAsLastInput;;
						relation.expectedOutput = appearences.getKey();
						relation.inputConfiguration = appearences.getValue().first.input;
						relations.add(relation);
					}
				} 
			}				
			
			///////////////////////////////////////////////////////////////////////////////
			// 2. inspect parentOutput -> outputRelations
			// in meaning of always same or oposite
			
			ArrayList<Data> neuronVals = vals.get(neuron);			
			ArrayList<Data> operatorOutputs = outputs.get(neuron);
			
			Data result = baseInstance.findNeuron(new StringData(NeuroHelper.HAS_SAME_ELEMENTS))
					.compute( new NeuronData(Analyser.EQ_OPER), CommonHelper.mergeCopy(new ArrayData(neuronVals), new ArrayData(operatorOutputs)));
			
			if( result != null && result.isTrue()) {
				RelationsData relation = new RelationsData();
				relation.processingNeuron = neuron;
				relation.relationNeuron = baseInstance.same;		
				relation.expectedUsedAsLastInput = false;
				relation.inputConfiguration = DataPredicate.cover(input.size());
				relations.add(relation);
				
				/*ArrayList<Data> inputMapping = new ArrayList<>();				
				for(int i = 0; i < input.size(); ++ i) {
					inputMapping.add(new IndexData(i));
				}
				try {
					InvokationData data = new InvokationData(new NeuronData(neuron), inputMapping);
					relation.invocation = data;
					addRelations(relation.relationNeuron, data);
				} catch (Exception e) {
				}*/
			}
			
			boolean allOpposite = true;
			for(int ind = 0, len = neuronVals.size(); ind < len && allOpposite;  ++ ind) {
				try {
					BoolData val = new BoolData(neuronVals.get(ind).toString());
					BoolData operatorOutput = new BoolData(operatorOutputs.get(ind).toString());
					
					allOpposite &= (val.compareTo(operatorOutput) != 0);
				} catch (Exception e) {
					allOpposite = false;
				}
				
			}
			
			if(allOpposite) {
				RelationsData relation = new RelationsData();
				relation.processingNeuron = neuron;
				relation.relationNeuron = baseInstance.opposite;	
				relation.expectedUsedAsLastInput = false;
				relation.inputConfiguration = DataPredicate.cover(input.size());
				relations.add(relation);
			}
			
			
			/////////////////////////////////////////////////////////////////////////////////
			// 3. inspect key->val relations
			// sometimes if for output 1 always expected x1, for output 2 always expected x2, ... to model output1 and 2, can be used x1, x2.
			HashMap<Data, HashMap<Data, Integer> > valueFromResult = new HashMap<>();
			HashMap<Data, HashMap<Data, Integer> > resultFromValue = new HashMap<>();
			
			for(int ind = 0, len = neuronVals.size(); ind < len;  ++ ind) {
				Data value = operatorOutputs.get(ind);
				Data nresult = neuronVals.get(ind);
				
				addResult(valueFromResult, value, nresult);
				addResult(resultFromValue, nresult, value);				
			}
			
			/**
			 * result computation relations
			 */
			for(Entry<Data, HashMap<Data, Integer>> valsPerResultEntry: valueFromResult.entrySet()) {
				if(valsPerResultEntry.getValue().size() == 1) {
					if(valsPerResultEntry.getKey() != null && valsPerResultEntry.getValue().values().iterator().next() >= SIMILARITY_LEARN_RATE) {
						// != null to exclude weird operators checks
						// require counter > 1 to exclude just 1 case examples (that not has power)
						// means all current value of neuron always lead to same operator output 
						RelationsData relation = new RelationsData();
						relation.processingNeuron = neuron;
						relation.expectedUsedAsLastInput = false;
						relation.inputConfiguration = DataPredicate.cover(input.size());
						relation.expectedOutput = valsPerResultEntry.getKey();
						// next is expected answer
						relation.expectedParentOutput = valsPerResultEntry.getValue().keySet().iterator().next();
						relation.relationNeuron = baseInstance.same;
						relations.add(relation);
					}					
				}
			}
			
			/**
			 * verification relations
			 */
			for(Entry<Data, HashMap<Data, Integer>> resultPerValEntry: resultFromValue.entrySet()) {
				if(resultPerValEntry.getValue().size() == 1) {
					Data expected = resultPerValEntry.getValue().keySet().iterator().next();
					if(expected != null && resultPerValEntry.getValue().values().iterator().next() >= SIMILARITY_LEARN_RATE) {
						// != null to exclude weird operators checks
						// require counter > 1 to exclude just 1 case examples (that not has power)
						// means when this output, it leads to same value of operator
						RelationsData relation = new RelationsData();
						relation.processingNeuron = neuron;
						relation.expectedUsedAsLastInput = false;
						relation.inputConfiguration = DataPredicate.cover(input.size());
						relation.expectedOutput = expected;
						// next is expected answer
						relation.expectedParentOutput = resultPerValEntry.getKey();
						relation.relationNeuron = baseInstance.validation;
						relations.add(relation);
					}					
				}
			}
 		}
				
		if(! knowledge.validateRelations(relations) ) {
			return;
		}
		
		if(knowledge.knowRelations()) {
			// method probably stable.
			
			// destroy kids for case it changed
			// XXX validate if those kids are the same later
			destroyKids();
			destroyRelations();
			
			Set<NeuronData> newKids = baseInstance.analyseNeuron(this);
			
			if(newKids != null) {
				for (NeuronData neuronData : newKids) {
					addKid(neuronData.getNeuron());
				}
			}
		}
		
		// if relations are changed after new experience added, check them.
		
		
		// make cross validation
		int passedNum = 0;
		int allNum = 0;
		Map<ArrayList<Data>, ExperienceSet> exp = knowledge.getInputExperience();
		
		for (Entry<ArrayList<Data>, ExperienceSet> expEntry : exp.entrySet()) {
			for(Data outputData: expEntry.getValue().experienceByOutput.keySet()) {
				++allNum;
				Data crossValidationData = computeEx(toData(), expEntry.getKey());
				
				if(crossValidationData == null || crossValidationData.compareTo(outputData) != 0) {
					/*// method is not trained well yet. restrict from computing by self
					knowledge.relations = null;
					// need to destroy kids
					destroyKids();
					destroyRelations();*/
					getLogger().info("Failed validation for: " + getName() + "(" + expEntry.getKey() + ") = " + outputData + " . receiced out: " + crossValidationData);
				} else { 
					++ passedNum;
				}
			}
		}	
		
		getLogger().info("Cross validation: passed " + passedNum + "/" + allNum);
	}

	private void addResult(
			HashMap<Data, HashMap<Data, Integer>> valueFromResult, Data value,
			Data nresult) {
		if(!valueFromResult.containsKey(nresult)) {
			valueFromResult.put(nresult, new HashMap<Data, Integer>());
		}				
		
		HashMap<Data, Integer> mapOfResults = valueFromResult.get(nresult);
		
		if(!mapOfResults.containsKey(value)) {
			mapOfResults.put(value, 1);
		} else {
			mapOfResults.put(value, 1 + mapOfResults.get(value));
		}
	}
	
	public void train(NeuronData operator, ArrayList<Data> input, Data output) {
		getLogger().info("train : " + getName() + "(" + input + ") = " + output);
		ArrayList<ResultExperience> experience = new ArrayList<>();
		
		NeuroBase base = NeuroBase.getInstance();
		boolean isOutputBoolean = (Data.construct(output.toString()) instanceof BoolData);
		boolean outputUsedAsLastInput = !isOutputBoolean;
		
		ArrayList<Data> mergedInput = input;
		
		if(outputUsedAsLastInput) {
			mergedInput = CommonHelper.mergeCopy(input, output);
		}
		
		for(Entry<Data, Neuron> neuron: base.getComputationNeurons().entrySet()) {
			// search input + output characteristics. If found => biective function can be used to find output from input
			// TODO check for all subsets of input. Sometimes actual value can be computed on subset, and other need only for validation.
			// example for above: similar length of containing string is just length of second, and verification is contains for both.
			Neuron neuronObj = neuron.getValue();
			
			if(neuronObj.compareTo(this) == 0) {
				continue;
			}
			
			Data result = neuronObj.compute(operator, mergedInput);			
			
			if(result != null) {
				//experience.put(neuron.getValue(), result);
				ResultExperience currentExperience = new ResultExperience(neuronObj, DataPredicate.cover(input.size(), outputUsedAsLastInput), result, output);
				currentExperience.expectedUsedAsLastInput = outputUsedAsLastInput;
				experience.add(currentExperience);
			} else {
				ResultExperience currentExperience = new ResultExperience(neuronObj, DataPredicate.cover(input.size(), outputUsedAsLastInput), null, output);
				currentExperience.expectedUsedAsLastInput = outputUsedAsLastInput;
				experience.add(currentExperience);
			}
			
			// TODO find verification relations by comparing output. 
			
			// TODO find exact same result relations 
			Data sameResult = neuronObj.compute(operator, input);
			if(sameResult != null && sameResult.compareTo(output) == 0) {
				if(outputUsedAsLastInput) {
					// other will be included before
					ResultExperience currentExperience = new ResultExperience(neuronObj, DataPredicate.cover(input.size(), false), result, output);
					experience.add(currentExperience);
				}
			}
		}
		
		setKnowledge(input, output, new IntegerData(new Date().getTime()), experience);	
		trainRelations(input, output);
		base.analyseExperience(this, operator, input, output);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("[" + getName() + "]:\n" );		 
		builder.append("\t" + knowledge.toString() + "\n");
		//builder.append("\toperators: " + relations.toString() + "\n");
		
		return builder.toString();
	}
	
	@Override
	public int hashCode() {
		return getName().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return compareTo((Neuron) obj) == 0;
	}

	@Override
	public int compareTo(Neuron o) {
		return getName().compareTo(o.getName());
	}

	/**
	 * @return the logger
	 */
	public Logger getLogger() {
		return logger;
	}
	
	public NeuronData toData() {
		return new NeuronData(this);
	}
}
