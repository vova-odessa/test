package nntest2.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import nntest2.Analyser;
import nntest2.data.ComputationStatistic.DataPredicate;
import nntest2.herpers.CommonHelper;
import nntest2.neurons.Neuron;

public class InvokationData extends Data {
	private static final int MAX_DEPTH = 100;
	public NeuronData neuron = null;
	public ArrayList<Data> inputMapping = null;
	public Data expectedValue = null;
	public NeuronData validationOperator = null; // by default will be used == operator. But it can be extended in future. Ex: (in) operator or (!=), etc.
	public NeuronData subjectNeuron = null;
	public Set<InvokationData> verificationCalls = new HashSet<>();
	
	/**
	 * For validation neurons, if call invoke instead of validate, they will return this value if not null.
	 * Needs for input <- validation sequence mapping 
	 */
	public Data resultData = null;
	
	public InvokationData(NeuronData neuron, ArrayList<Data> inputMapping) throws Exception {
		if(neuron == null || inputMapping == null) {
			throw new Exception();
		}
		this.neuron = neuron;
		this.inputMapping = inputMapping;
	}
	
	public void addVerificationCall(InvokationData invocation) {
		verificationCalls.add(invocation);
	}
	
	public InvokationData clone() {
		try {
			InvokationData data = new InvokationData(neuron, new ArrayList<>(inputMapping), expectedValue);
			data.resultData = resultData;
			data.subjectNeuron = subjectNeuron;
			data.verificationCalls = new HashSet<>(verificationCalls);
			data.validationOperator = validationOperator;
			return data;
		} catch (Exception e) {
			return null;
		}
	}
	
	public InvokationData(NeuronData neuron, ArrayList<Data> inputMapping, Data expected) throws Exception {
		if(neuron == null || inputMapping == null) {
			throw new Exception();
		}
		this.neuron = neuron;
		this.inputMapping = inputMapping;
		expectedValue = expected;
	}
	
	public Data invoke(ArrayList<Data> parentMethodInput, Data parentOutput, HashMap<Data, Data> context) {
		Data result = cleanInvoke(parentMethodInput, parentOutput, context);
		
		if(result == null) {
			return null;
		}
		
		if(expectedValue != null) {
			// resolvation is need when expected depends from parent output or input and is not constant.
			Data resolvedExpected = resolveInput(parentMethodInput, parentOutput, context, expectedValue);
			
			boolean isResultVerificationSuccess = isResultMatchesExpactations(result, resolvedExpected);
			
			
			if(isResultVerificationSuccess) {
				// if there are some value expected
				// result equal to expected
				if(resultData != null) {
					// means all before was just check for this answer. And it is passed.
					return resultData;
				}
			} else {
				// if expected not passed, return is null
				return null;
			}
		}
		
		if(resultData != null) {
			// means expected = 0, but result != null.
			// that not have sense currently
			return null;
		}
		
		return result;
	}

	private boolean isResultMatchesExpactations(Data result, Data resolvedExpected) {
		boolean isResultVerificationSuccess = false;
		
		if(validationOperator == null || !validationOperator.isValid()) {
			// if no operator set, use just == to compare.
			isResultVerificationSuccess = result.compareTo(resolvedExpected) == 0;
		} else {
			// if operator is set, use it.
			isResultVerificationSuccess = validationOperator.getNeuron().compute(null, CommonHelper.mergeCopy(result, resolvedExpected) ).isTrue();
		}
		return isResultVerificationSuccess;
	}

	private Data cleanInvoke(ArrayList<Data> parentMethodInput, Data parentOutput, HashMap<Data, Data> context) {
		if(Thread.currentThread().getStackTrace().length > MAX_DEPTH) {
			// if so, probably there are computation lock
			return null;
		}
		
		
		ArrayList<Data> realInput = resolveInput(parentMethodInput, parentOutput, context);
		
		if(realInput == null) {
			return null;
		}
		
		Data result = neuron.getNeuron().compute(neuron, realInput);
		
		if(result == null) {
			return null;
		}
		
		// do verification
		for (InvokationData verificationMethod : verificationCalls) {
			//verificationMethod.expectedValue = result;
			if(!verificationMethod.validate(parentMethodInput, result, context)) {
				return null;
			}
		}
		return result;
	}
	
	public ArrayList<Data> resolveInput(ArrayList<Data> parentMethodInput, Data parentOutput, HashMap<Data, Data> context) {		
		if(inputMapping == null) {
			return null;
		}
		ArrayList<Data> result = new ArrayList<Data>();
		
		for(int i = 0, len = inputMapping.size(); i < len; ++ i) {
			Data curr = inputMapping.get(i);
			Data resolved = resolveInput(parentMethodInput, parentOutput, context, curr);
			
			if(resolved == null) {
				return null;
			}
			result.add(resolved);
		}
		
		return result;
	}

	private Data resolveInput(ArrayList<Data> parentMethodInput, Data parentOutput, HashMap<Data, Data> context, Data inputValue) {
		Data resolved = null;
		
		if(inputValue instanceof IndexData) {
			int ind = ((IndexData) inputValue).index;
			if(ind >= 0 && parentMethodInput.size() > ind) {
				try {
					resolved = parentMethodInput.get(ind);
				} catch(Throwable t) {
					if(neuron != null) {
						//neuron.getNeuron().getLogger().error("InvokationData: No element with index [" + ind + "] in parent input: " + t.getMessage());
					}
				}
			} else if(ind == DataPredicate.OUTPUT_INDEX) {
				resolved = parentOutput;
			} else {
				if(neuron != null) {
					//neuron.getNeuron().getLogger().error("InvokationData: No element with index [" + ind + "] in parent input");
				}
			}
		} else if(inputValue instanceof InvokationData) {
			resolved = ((InvokationData) inputValue).invoke(parentMethodInput, parentOutput, context) ;
		} else {
			resolved = inputValue;
		}
		return resolved;
	}
	
	public boolean validate(ArrayList<Data> parentMethodInput, Data parentOutput, HashMap<Data, Data> context) {		
		if(expectedValue == null) {
			// that is wrong filled validation. It should not work
			return true;
		}
		
		if(parentOutput != null && resultData != null && parentOutput.compareTo(resultData) != 0
				|| resultData != null && parentOutput == null) {
			// means if this validation works only for some output value, skip with other result
			return true;
		}
		
		// resolvation is need when expected depends from parent output or input and is not constant.
		Data resolvedExpected = resolveInput(parentMethodInput, parentOutput, context, expectedValue);
		
		if(resolvedExpected == null) {
			return false;
		}
		
		Data result = //invoke(parentMethodInput, parentOutput, context);
				cleanInvoke(parentMethodInput, parentOutput, context);
		
		if(result == null) {
			return false;
		}
		
		// if there are some value expected
		return isResultMatchesExpactations(result, resolvedExpected);
	}
	
	@Override
	public String toString() {
		StringBuilder resultBuilder = new StringBuilder();
		resultBuilder.append("{");
		
		if(neuron != null) {
			resultBuilder.append("\tneuron = " + neuron.getNeuron().getName());
		}
		
		if(inputMapping != null) {
			resultBuilder.append("(" + inputMapping.toString() + ")");
		}
		
		if(expectedValue != null) {
			resultBuilder.append(" expected result is (" + expectedValue.toString() + ")");
		}
		
		if(subjectNeuron != null) {
			resultBuilder.append(", relation: " + subjectNeuron.getNeuron().getName());
		}
		
		if(resultData != null) {
			resultBuilder.append(", result if passed = " + resultData);
		}
		
		resultBuilder.append("}");
		
		
		return resultBuilder.toString();
	}
}
