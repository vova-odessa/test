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
import nntest2.data.TextData;
import nntest2.herpers.CommonHelper;
import nntest2.herpers.NeuroHelper;
import nntest2.neurons.Neuron;

public class NeuroBase {
	Logger logger = Logger.getLogger(NeuroBase.class);
	//private static final StringData LENGTH_NEURON_NAME = new StringData("length");
	//private static final StringData SAME_ELEMENTS_NEURON_NAME = new StringData("same elements");
	
	public Neuron bijection = new Neuron("neuron bijection analysis") {
		
	};
		
	public Neuron invariant = new Neuron("neuron invariant analysis") {
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
						|| relation.expectedOutput != null && !(relation.expectedOutput instanceof BoolData) && relation.expectedParentOutput == null) {
					try {
						// if there expected output for neuron. We can use bijection function of that neuron to compute value.
						// f(x, y) = z, assume it neuron, and it have expected z constant. x - normal of F(x) = y, that we want to know 
						// bijection g(x, z) = y. When z is known and x is input, that method can be used to compute y.
						
						//invocations.add(new InvokationData(new NeuronData(relation.processingNeuron), DataPredicate.extract(relation.inputConfiguration)));						
						HashSet<InvokationData> rels = relation.processingNeuron.getRelations(bijection);
						
						// need add both - computation and verification cases.
						// Expected: task is y: F(x), known f(x, y) = z, z is const, and bijection g(x, z) = y.
						// 1) compute y: g(x, z), 
						// 2) verify f(x, y) = z.
						
						boolean bijectionAdded = false;
						
						if(rels != null) {
							for (InvokationData invokationData : rels) {
								//invokationData.inputMapping.set(invokationData.inputMapping.size() - 1, relation.expectedOutput);
								InvokationData data = invokationData.clone();
								data.inputMapping.set(invokationData.inputMapping.size() - 1, relation.expectedOutput);
								data.subjectNeuron = new NeuronData(relation.processingNeuron);
								
								if(data.neuron.getNeuron().compareTo( inputNeuron ) == 0) {
									continue;
								}
								
								invocations.add(data);
								
								if(relation.processingNeuron.compareTo( inputNeuron ) != 0) {
									InvokationData verificationCall = data.clone();
									verificationCall.inputMapping.set(invokationData.inputMapping.size() - 1, new IndexData(DataPredicate.OUTPUT_INDEX));
									verificationCall.expectedValue = relation.expectedOutput;
									verificationCall.neuron = new NeuronData(relation.processingNeuron);
									verificationCall.verificationCalls = new HashSet<>();
									verificationCall.resultData = null;
									verificationCall.validationOperator = null;
									
									data.addVerificationCall(verificationCall);
								}
								
								bijectionAdded = true;
							}
							
							// XXX check relations with existent data
						}
						
						if(relation.expectedUsedAsLastInput) {
							// can not do anything with that, only post validation.
							// XXX consider post validation relations
						} else	{	
							// verification that can be applied in both cases, are there bijection or not.
							inputNeuron.addRelations(NeuroBase.getInstance().validation, 
									new InvokationData(new NeuronData(relation.processingNeuron), 
											DataPredicate.extract(relation.inputConfiguration, null), relation.expectedOutput));
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
						InvokationData invocation = new InvokationData(new NeuronData(relation.processingNeuron), DataPredicate.extract(relation.inputConfiguration, null));
						
						if(relation.expectedOutput != null) {
							// this will make from invocation, validation neuron
							invocation.expectedValue = relation.expectedOutput;
						}
						
						if(relation.expectedParentOutput != null) {
							invocation.resultData = relation.expectedParentOutput;
						}
						
						invocations.add(invocation);
					} 	
				} catch (Exception e) {
				}
				
			}
			
			inputNeuron.addRelations(this, invocations);
			
			// no neurons are produced
			return null;
		}
		

		@Override
		public boolean isComputationRelation() {
			return true;
		}
	};
	
	public Neuron opposite = new Neuron("neuron opposite similarity analysis") {
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
						Neuron notNeuron = NeuroBase.getInstance().findNeuron(new StringData("(not)"));
						if(notNeuron != null) {
							ArrayList<Data> inputMapping = new ArrayList<Data>();
							inputMapping.add(new InvokationData(new NeuronData(relation.processingNeuron), DataPredicate.extract(relation.inputConfiguration, null)));
							InvokationData notInvocation = new InvokationData(new NeuronData(notNeuron), inputMapping);
							
							invocations.add(notInvocation);
						}
					} 	
				} catch (Exception e) {
				}
				
			}
			
			inputNeuron.addRelations(this, invocations);
			
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
						|| relation.expectedOutput != null && relation.expectedParentOutput == null && relation.expectedOutput instanceof BoolData) {
					try {
						InvokationData solution = new InvokationData(new NeuronData(relation.processingNeuron), DataPredicate.extract(relation.inputConfiguration, null), relation.expectedOutput);
						solution.resultData = relation.expectedParentOutput;
						
						invocations.add(solution);						
					} catch (Exception e) {
					}
				}
			}
			
			inputNeuron.addRelations(this, invocations);
			
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
	private HashMap<Data, Neuron> textAnalysingNeurons = new HashMap<>();
	private HashMap<Data, Neuron> computationNeurons = new HashMap<>();
	
	private NeuroBase() {
		// init basic possibilities
		
		HashMap<Data, Neuron> basisNeurons = NeuroHelper.createBaseNeurons(this);
		
		for(Neuron neuron: basisNeurons.values()) {
			register(neuron);
		}
		
		register(bijection);
		register(invariant);
		register(same);
		register(opposite);
		register(validation);
	}
	
	public Neuron getOperator(Data operator) {
		return null;
	}
	
	public Neuron validateNeuron(Data name) {
		Neuron result = findNeuron(name);
		if(result != null) {
			return result;
		}	
		
		if(name instanceof NeuronData ) {
			if(((NeuronData) name).isValid()) {
				return ((NeuronData) name).getNeuron();
			} else {
				result = new Neuron(((NeuronData) name).getNeuron().getName().toString());			
				return result.register();
			}
		} else {		
			result = new Neuron(name.toString());			
			return result.register();
		}
	}
	
	public Neuron findNeuron(Data name) {	
		return findNeuron(name, true);
	}
	
	public Neuron findNeuron(Data name, boolean doParameterisedSearch) {
		if(name instanceof NeuronData) {
			if(((NeuronData) name).isValid()) {
				return ((NeuronData) name).getNeuron();
			} else {
				return null;
			}
		}
		
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
	
	public HashMap<Data, Neuron> getComputationNeurons() {
		return computationNeurons;
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
		
		if(neuron.canAnalyseText()) {
			textAnalysingNeurons.put(neuron.getName(), neuron);
		}
		
		if(neuron.canBeUsedForGeneralComputation()) {
			computationNeurons.put(neuron.getName(), neuron);
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
	
	public void analyseExperience(Neuron inputNeuron, NeuronData operator, ArrayList<Data> input, Data output) {		
		for (Neuron neuron : neuronExperienceAnalysingNeurons.values()) {
			neuron.compute(inputNeuron, operator, input, output);
		}
	}
	
	public Data analyseText(TextData text) {
		ArrayList<Data> results = new ArrayList<>();
		
		for (Neuron neuron : textAnalysingNeurons.values()) {
			Data result = neuron.analyseText(text);
			if(result != null) {
				results.add(result);
			}
		}
		
		return TextData.constructPossibilities(results);
	}
}
