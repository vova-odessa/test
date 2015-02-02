package nntest2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.Loader;

import nntest2.data.BoolData;
import nntest2.data.ComputationStatistic.DataPredicate;
import nntest2.data.ComputationStatistic.RelationsData;
import nntest2.data.Data;
import nntest2.data.IndexData;
import nntest2.data.InvokationData;
import nntest2.data.NeuronData;
import nntest2.data.ProcessingResult;
import nntest2.data.StringData;
import nntest2.herpers.CommonHelper;
import nntest2.herpers.NeuroHelper;
import nntest2.neurons.Neuron;

public class NeuroBase {
	Logger logger = Logger.getLogger(NeuroBase.class);
	//private static final StringData LENGTH_NEURON_NAME = new StringData("length");
	//private static final StringData SAME_ELEMENTS_NEURON_NAME = new StringData("same elements");
	
	public Neuron bijection = new Neuron("neuron bijection analysis") {
		public boolean canAnalyseNeurons() {
			return true;
		};
		

		@Override
		public boolean isComputationRelation() {
			return true;
		}
		
		@Override
		public Set<NeuronData> compute(Neuron inputNeuron) {
			Set<InvokationData> invocations = new HashSet<>();
			
			for(RelationsData relation: inputNeuron.getRelations()) {
				if(relation.relationNeuron != null && relation.relationNeuron.compareTo(this) == 0 
						|| relation.expectedOutput != null && !(relation.expectedOutput instanceof BoolData)) {
					try {
						//invocations.add(new InvokationData(new NeuronData(relation.processingNeuron), DataPredicate.extract(relation.inputConfiguration)));						
						HashSet<InvokationData> rels = relation.processingNeuron.getRelations(bijection);
						
						if(rels != null) {
							for (InvokationData invokationData : rels) {
								//invokationData.inputMapping.set(invokationData.inputMapping.size() - 1, relation.expectedOutput);
								InvokationData data = invokationData.clone();
								data.inputMapping.set(invokationData.inputMapping.size() - 1, relation.expectedOutput);
								data.subjectNeuron = new NeuronData(relation.processingNeuron);
								invocations.add(data);								
							}
							
							// XXX check relations with existent data
						} else {
							inputNeuron.addRelations(NeuroBase.getInstance().validation, new InvokationData(new NeuronData(relation.processingNeuron), DataPredicate.extract(relation.inputConfiguration), relation.expectedOutput));
						}
					} catch (Exception e) {
					}
				}
			}
			
			inputNeuron.addRelations(this, invocations);
			
			// no neurons are produced
			return null;
		}		
	};
	
	public Neuron same = new Neuron("neuron similarity analysis") {
		public boolean canAnalyseNeurons() {
			return true;
		};
		
		@Override
		public Set<NeuronData> compute(Neuron inputNeuron) {
			//return super.compute(inputNeuron);
			Set<InvokationData> invocations = new HashSet<>();
			
			for(RelationsData relation: inputNeuron.getRelations()) {
				try {
					if(relation.relationNeuron != null && relation.relationNeuron.compareTo(this) == 0) {
						invocations.add(new InvokationData(new NeuronData(relation.processingNeuron), DataPredicate.extract(relation.inputConfiguration)));
					} else if(relation.expectedOutput != null && relation.expectedOutput instanceof BoolData) {					
						if(relation.expectedOutput.compareTo(relation.expectedOutput) == 0) {							
							invocations.add(new InvokationData(new NeuronData(relation.processingNeuron), DataPredicate.extract(relation.inputConfiguration)));							
						} else {
							Neuron notNeuron = NeuroBase.getInstance().findNeuron(new StringData("(not)"));
							if(notNeuron != null) {
								ArrayList<Data> inputMapping = new ArrayList<Data>();
								inputMapping.add(new InvokationData(new NeuronData(relation.processingNeuron), DataPredicate.extract(relation.inputConfiguration)));
								InvokationData notInvocation = new InvokationData(new NeuronData(notNeuron), inputMapping);
							}
						}	
					}				
				} catch (Exception e) {
				}
				
			}
			
			addRelations(this, invocations);
			
			// no neurons are produced
			return null;
		}
		

		@Override
		public boolean isComputationRelation() {
			return true;
		}
	};
	
	public Neuron validation = new Neuron("neuron validation analysis") {
		public boolean canAnalyseNeurons() {
			return true;
		};
		
		@Override
		public Set<NeuronData> compute(Neuron inputNeuron) {
			Set<InvokationData> invocations = new HashSet<>();
			
			for(RelationsData relation: inputNeuron.getRelations()) {
				if(relation.relationNeuron != null && relation.relationNeuron.compareTo(this) == 0 
						|| relation.expectedOutput != null && relation.expectedOutput instanceof BoolData) {
					try {
						invocations.add(new InvokationData(new NeuronData(relation.processingNeuron), DataPredicate.extract(relation.inputConfiguration), relation.expectedOutput));						
					} catch (Exception e) {
					}
				}
			}
			
			addRelations(this, invocations);
			
			// no neurons are produced
			return null;
		}		

		public boolean isVerificationRelation() {
			return true;
		};
	};
	
	private HashMap<Data, Neuron> neurons = new HashMap<>();
	private HashMap<Data, Neuron> parameterisedNeurons = new HashMap<>();
	private HashMap<Data, Neuron> neuronAnalysingNeurons = new HashMap<>();
	private HashMap<Data, Neuron> neuronExperienceAnalysingNeurons = new HashMap<>();
	
	private NeuroBase() {
		// init basic possibilities
		
		HashMap<Data, Neuron> basisNeurons = NeuroHelper.createBaseNeurons(this);
		
		for(Neuron neuron: basisNeurons.values()) {
			register(neuron);
		}
		
		register(bijection);
		register(same);
		register(validation);
	}
	
	public Neuron getOperator(Data operator) {
		return null;
	}
	
	/**
	 * key - processing neuron
	 * value - expecting result
	 * 
	 * 
	 * @param neuroCombination
	 * @return
	 */
	public Neuron validateNeuron(HashMap<Neuron, Data> neuroConditionCombination, Set<Data> correctResultLabels, Set<Data> wrongResultLabels) {
		return null;
	}
	
	public Neuron validateNeuron(Data name) {
		Neuron result = findNeuron(name);
		if(result != null) {
			return result;
		}	
		
		result = new Neuron(name.toString());
		
		return result.register();
	}
	
	public Neuron findNeuron(Data name) {	
		return findNeuron(name, true);
	}
	
	public Neuron findNeuron(Data name, boolean doParameterisedSearch) {		
		Neuron result = neurons.get(name);
		
		if(result == null && doParameterisedSearch) {
			// try to find parameterised one
			
			// TODO later need to find by hash value. That should be pre - computed for name. This will improve dramatically this part.
			for(Neuron neuron: parameterisedNeurons.values()) {
				if(neuron.validate(name.toString())) {
					return neuron;
				}
			}
		}
		
		return result;
	}
	
	public ProcessingResult process(Data input) {
		return null;
	}
	
	private static NeuroBase instance = new NeuroBase();
	public static NeuroBase getInstance() {
		return instance;
	}

	/**
	 * @return the neurons
	 */
	public HashMap<Data, Neuron> getNeurons() {
		return neurons;
	}
	
	public Neuron register(Neuron neuron) {
		if(neurons.containsKey(neuron.getName())) {
			logger.error("Neuron already exist with name: " + neuron.getName());
			return neurons.get(neuron.getName());
		}
		
		logger.info("Added neuron : [" + neuron.getName() + "]");
		
		neurons.put(neuron.getName(), neuron);
		
		if(neuron.canValidate()) {
			parameterisedNeurons.put(neuron.getName(), neuron);
		}
		
		if(neuron.canAnalyseNeurons()) {
			neuronAnalysingNeurons.put(neuron.getName(), neuron);
		}
		
		if(neuron.canAnalyseResults()) {
			neuronExperienceAnalysingNeurons.put(neuron.getName(), neuron);
		}
		
		return neuron;
	}
	
	public void destroy(Neuron neuron) {
		logger.info("Destroyed neuron : [" + neuron.getName() + "]");
		
		neurons.remove(neuron.getName());
		parameterisedNeurons.remove(neuron.getName());
		neuronAnalysingNeurons.remove(neuron.getName());
	}
	
	public Set<NeuronData> analyseNeuron(Neuron inputNeuron) {
		HashSet<NeuronData> resultNeurons = new HashSet<>();
		
		for (Neuron neuron : neuronAnalysingNeurons.values()) {
			Set<NeuronData> data = neuron.compute(inputNeuron);
			
			if(data != null) {
				resultNeurons.addAll(data);
			}
		}
		
		return resultNeurons;
	}
	
	public void analyseExperience(Neuron inputNeuron, StringData operator, ArrayList<Data> input, Data output) {		
		for (Neuron neuron : neuronExperienceAnalysingNeurons.values()) {
			neuron.compute(inputNeuron, operator, input, output);
		}
	}
}
