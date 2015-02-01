package nntest2.neurons;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import nntest2.NeuroBase;
import nntest2.data.AnalysisRequest;
import nntest2.data.BoolData;
import nntest2.data.ComputationStatistic;
import nntest2.data.ComputationStatistic.DataPredicate;
import nntest2.data.ComputationStatistic.ExperienceData;
import nntest2.data.ComputationStatistic.ExperienceSet;
import nntest2.data.ComputationStatistic.RelationsData;
import nntest2.data.ComputationStatistic.ResultExperience;
import nntest2.data.Data;
import nntest2.data.IntegerData;
import nntest2.data.NeuronData;
import nntest2.data.StringData;
import nntest2.herpers.CommonHelper;

import org.apache.log4j.Logger;

import com.sec.gb.ipa.ks.common.data.Pair;

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
	
	public Set<NeuronData> compute(Neuron inputNeuron) {
		return null;
	}
	
	public void compute(Neuron inputNeuron, StringData operator, ArrayList<Data> input, Data output) {
		compute(new AnalysisRequest(inputNeuron, operator, input, output));
	}
	
	public void compute(AnalysisRequest request) {
		
	}
	
	public Data compute(StringData operatorName, Data input) {	
		return findAnswer(operatorName, CommonHelper.mergeCopy(input));
	}

	private Data findAnswer(StringData operatorName, ArrayList<Data> input) {
		Set<Data> prevAnswers = getKnowledge(input, null);
		
		if(prevAnswers.size() == 1) {
			// have only 1 unambiguous answer
			return prevAnswers.iterator().next();
		}
		
		return computeEx(operatorName, input);
	}
	
	public Set<RelationsData> getRelations() {
		return knowledge.relations;
	}
	
	public Data computeEx(StringData operatorName, ArrayList<Data> input) {
		return computeEx(operatorName, input, null);
	}

	/**
	 * This method computes using logic (no memory data)
	 * @param input 
	 * @return
	 */
	public Data computeEx(StringData operatorName, ArrayList<Data> input, HashMap<Neuron, Data> alternativeData) {
		// try to find answer
		
		Set<Data> returnedData = new HashSet<>();
		// collect possibilities from neurons with bijection neurons
		if(knowledge.knowRelations()) {
			//for (Entry<Neuron, Pair<Data, Double>> data : acceptedConfiguration.entrySet()) {
			for(RelationsData data: knowledge.relations) {
				//Neuron checkNeuron = data.getKey();
				Neuron checkNeuron = data.processingNeuron;
				Neuron bijectionNeuron = checkNeuron.bijectionNeuron();
				
				if(bijectionNeuron != null) {
					//Data neuronValue = data.getValue().first;
					Data neuronValue = data.actualOutput;
					
					if(alternativeData != null && alternativeData.containsKey(data.processingNeuron)) {
						// use suggested data. Need for variables
						neuronValue = alternativeData.get(data.processingNeuron);
					}
					
					Data answer = bijectionNeuron.compute(operatorName, CommonHelper.mergeCopy(input, neuronValue));
					
					// check answer on all checking features
					boolean passed = true;
					//for (Entry<Neuron, Pair<Data, Double>> controlData : acceptedConfiguration.entrySet()) {
					for(RelationsData controlData: knowledge.relations) {
						Neuron conditionNeuron = controlData.processingNeuron;
						Neuron nullNeuron = conditionNeuron.bijectionNeuron();
						
						if(nullNeuron == null) {
							// if there no bijection neuron, relation can still be used to validate
							Data normalValue = controlData.actualOutput;
							if(alternativeData != null && alternativeData.containsKey(controlData.processingNeuron)) {
								normalValue = alternativeData.get(data.processingNeuron);
							}
							
							Data answer2 = conditionNeuron.compute(operatorName, CommonHelper.mergeCopy(input, answer), normalValue);
							if(answer2 == null || !answer2.isTrue()) {
								logger.info("answer is omited [" + answer + "] because [" + conditionNeuron.getName() + "] = [" + normalValue + "] not passed" ); 
								passed = false;
								break;
							}
						}
					}				
					
					if(passed) {
						returnedData.add(answer);
					}
				}
			}
			
			// remove null answers if are
			returnedData.remove(null);
			
			if(returnedData.size() == 1) {
				return returnedData.iterator().next();
			}
			
			// no answer or answer is ambiguous
		}
		
		return null;
	}
	
	public Data compute(StringData operatorName, Data input, Data output) {
		Data result = compute(operatorName, input);
		return new BoolData( result != null && result.compareTo(output) == 0 );
	}
	
	public Data compute2(StringData operatorName, Data input1, Data input2) {
		return findAnswer(operatorName, CommonHelper.mergeCopy(input1, input2));
	}
	
	public Data compute2(StringData operatorName, Data input1, Data input2, Data output) {
		Data result = compute2(operatorName, input1, input2);
		return new BoolData( result != null && result.compareTo(output) == 0 );
	}
	
	public Neuron bijectionNeuron() {
		return null;
	}

	public Data compute(StringData operatorName, ArrayList<Data> input) {
		Data result = null;
		if(input.size() == 1) {
			result = compute(operatorName, input.get(0));
		} else if(input.size() == 2) {
			result = compute2(operatorName, input.get(0), input.get(1));
		} else {
			Data answer = findAnswer(operatorName, input);
			
			if(answer != null) {
				result = answer;
			} else {			
				result = computeEx(operatorName, input);
			}
		}
		
		if(result != null) {
			// make analysis of computed results
			NeuroBase.getInstance().analyseExperience(this, operatorName, input, result);
		}
		
		return result;
	}
	
	public Data compute(StringData operatorName, ArrayList<Data> input, Data output) {
		Data result = null;
		if(input.size() == 1) {
			result = compute(operatorName, input.get(0), output);			
		} else if(input.size() == 2) {
			result = compute2(operatorName, input.get(0), input.get(1), output);
		}
		
		if(result != null) {
			return result;
		}
		
		result = compute(operatorName, input);
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
		
		////////////////////////////////////////////
		// compute invariant statistic 
		HashSet<RelationsData> invariants = new HashSet();
		// normalize statistic
		for(Entry<Neuron, HashMap<Data, Pair<ResultExperience, Double>>> answerStats: distribution.entrySet() ) {
			
			for( Entry<Data, Pair<ResultExperience, Double>> appearences: answerStats.getValue().entrySet() ) {
				if(appearences.getValue().second > INVARIANT_LEARN_ACCEPT_TRESHOLD) {
					//invariants.add(appearences.getKey());
					RelationsData relation = new RelationsData();
					relation.processingNeuron = answerStats.getKey();
					relation.expectedOutput = output;
					relation.actualOutput = appearences.getKey();
					relation.inputConfiguration = appearences.getValue().first.inputConfiguration;
					//TODO relation.expectedUsedAsLastInput = 
					invariants.add(relation);
				} 
			}				
		}	
		
		if(! knowledge.validateRelations(invariants) ) {
			return;
		}
		
		// if relations are changed after new experience added, check them.
		
		// make cross validation
		Data crossValidationData = computeEx(getName(), input);
		
		if(crossValidationData == null || crossValidationData.compareTo(output) != 0) {
			// method is not trained well yet. restrict from computing by self
			knowledge.relations = null;
			// need to destroy kids
			destroyKids();
		}		
		
		if(knowledge.knowRelations()) {
			// method probably stable.
			
			// destroy kids for case it changed
			// XXX validate if those kids are the same later
			destroyKids();
			
			Set<NeuronData> newKids = NeuroBase.getInstance().analyseNeuron(this);
			
			if(newKids != null) {
				for (NeuronData neuronData : newKids) {
					addKid(neuronData.getNeuron());
				}
			}
		}
	}
	
	public void train(StringData operatorName, ArrayList<Data> input, Data output) {
		ArrayList<ResultExperience> experience = new ArrayList<>();
		
		NeuroBase base = NeuroBase.getInstance();
		ArrayList<Data> mergedInput = CommonHelper.mergeCopy(input, output);
		
		for(Entry<Data, Neuron> neuron: base.getNeurons().entrySet()) {
			// search input + output characteristics. If found => biective function can be used to find output from input
			// TODO check for all subsets of input. Sometimes actual value can be computed on subset, and other need only for validation.
			// example for above: similar length of containing string is just length of second, and verification is contains for both.
			Data result = neuron.getValue().compute(operatorName, mergedInput);
			if(result != null) {
				//experience.put(neuron.getValue(), result);
				ResultExperience currentExperience = new ResultExperience(neuron.getValue(), DataPredicate.cover(mergedInput), result);
				experience.add(currentExperience);
			}
			
			// TODO find verification relations by comparing output. 
			
			// TODO find exact same result relations 
			Data sameResult = neuron.getValue().compute(operatorName, input);
			if(sameResult != null && sameResult.compareTo(output) == 0) {
				// XXX
			}
		}
		
		setKnowledge(input, output, new IntegerData(new Date().getTime()), experience);	
		trainRelations(input, output);
		base.analyseExperience(this, operatorName, input, output);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("[" + getName() + "]:\n" );		 
		builder.append("\t" + knowledge.toString() + "\n");
		
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
}
