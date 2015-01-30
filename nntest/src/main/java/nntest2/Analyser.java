package nntest2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;

import nntest2.data.ArrayData;
import nntest2.data.Data;
import nntest2.data.Fact;
import nntest2.data.StringData;
import nntest2.data.TextData;
import nntest2.neurons.Neuron;

public class Analyser {
	Logger logger = Logger.getLogger(Analyser.class);
	
	public static final StringData EQ_OPER = new StringData("(=)");
	public static final StringData THEN_OPER = new StringData("(=>)");
	public static final StringData THEN_ONLY_THEN_OPER = new StringData("(<=>)");
	private final Set<StringData> defaultOperators = new HashSet<>(Arrays.asList(new StringData[] {EQ_OPER, THEN_OPER, THEN_ONLY_THEN_OPER}));
	private Set<StringData> extendedOperators = new HashSet<>();
	//private HashMap<Data, Data> labels = new HashMap<>();
	private Queue<Fact> prevFacts = new LinkedList<>();
	private int maxMemorySize = 100;
	
	public Data Analyse(Fact fact) {
		Fact left = extractFactEx(fact.first.toString());
		
		ArrayList<Data> inputList = new ArrayList<>();
		if(left.operator.toString().length() > 0) {
			// try to learn it
			if(left.first.toString().length() > 0) {
				inputList.addAll(Arrays.asList(ArrayData.splitToElements(left.first.toString())));
			}
			if(left.second.toString().length() > 0) {
				inputList.addAll(Arrays.asList(ArrayData.splitToElements(left.second.toString())));
			}
		}
		
		if(fact.operator != null && fact.operator.compareTo(EQ_OPER) == 0) {
			if(inputList.size() > 0) {
				// has training operator, train data
				Neuron neuron = NeuroBase.getInstance().validateNeuron(left.operator);
				neuron.train(fact.operator.toStringData(), inputList, fact.second);
				
				logger.info(neuron.toString());
				
				Data crossValidationData = neuron.computeEx(fact.operator.toStringData(), inputList);
				
				if(crossValidationData == null || crossValidationData.compareTo(fact.second) != 0) {
					logger.info("Cross validation failed. Need more data");
				} else {
					logger.info("Cross validation success. Neuron trained");
				}
			} else {
				// make assotiation
			}
		} else if(fact.operator == null || fact.operator.elements().length == 0) {
			// no global training operator, consider as request
			if(inputList.size() > 0) {
				Neuron neuron = NeuroBase.getInstance().findNeuron(left.operator);
				if(neuron != null) {
					return neuron.compute(left.operator.toStringData(), inputList);
				} else {
					logger.error("Do not know how to perform (" + left.operator + ") please provide example" );
				}
			}
		}
		
		return null;
	}
	
	public void Analyse(String text) {
		logger.info("Q: " + text);
		Fact fact = extractBaseFact(text);
		
		if(fact != null) {
			Data answer = Analyse(fact);
			
			if(answer != null) {
				logger.info("\nQ[" + text + "] A = [" + answer + "]");
			}
		}
		// TODO consider further analysis 
	}

	private Fact extractBaseFact(String text) {
		HashSet<StringData> operators = new HashSet<>(defaultOperators);
		operators.addAll(extendedOperators);
		
		StringData data = new StringData(text);
		Data[] elements = null;
		
		for (StringData operator : operators) {
			if( text.contains(operator.toString()) ) {
				elements = data.split(operator);
				break;
				// that is for only 1 operator case.
				// TODO: consider several operators in future
			}
		}
		if(elements == null) {
			elements = new Data[]{new StringData(text)};
		}
		
		Fact fact = new Fact();
		
		if(elements.length > 0) {
			fact.first = elements[0];
		}
		
		if(elements.length > 1) {
			fact.operator = elements[1];
		}
		
		if(elements.length > 2) {
			fact.second = elements[2];
		}
		return fact;
	}
	
	private Fact extractFactEx(String text) {		
		StringData data = new StringData(text);
		Data[] elements = null;
		
		int openBrIndex = text.indexOf("(");		
		int closeBrIndex = text.indexOf(")");
		
		if( openBrIndex < closeBrIndex && openBrIndex != -1) {
			elements = new Data[3];
			elements[0] = new StringData(text.substring(0, openBrIndex).trim());
			elements[1] = new StringData(text.substring(openBrIndex + 1, closeBrIndex).trim());
			elements[2] = new StringData(text.substring(closeBrIndex + 1).trim());
		}
		
		if(elements == null) {
			elements = new Data[]{new StringData(text)};
		}
		
		Fact fact = new Fact();
		
		if(elements.length == 3) {
			// operator is found 
			fact.first = elements[0];
			fact.operator = elements[1];
			fact.second = elements[2];
		} else {
			fact = new Fact();
			fact.first = elements[0];
		}
		return fact;
	}
	
	public static void main(String[] args) {
		Analyser analyser = new Analyser();
		
		// last N
		analyser.Analyse("(last 2 of) 12345 (=) 45");
		analyser.Analyse("(last 2 of) 1234567 (=) 67");
		analyser.Analyse("(last 2 of) aabbvccd (=) cd");
		analyser.Analyse("(last 2 of) aabbaavccd (=) cd");
		
		// starts with
		analyser.Analyse("(starts with) aabbaavccd aab (=) true");
		analyser.Analyse("(starts with) aabbaavccd aabb (=) true");
		analyser.Analyse("(starts with) aabbaavccd aabba (=) true");
		analyser.Analyse("(starts with) aabbaavccd aabbaa (=) true");
		
		// first N
		analyser.Analyse("(first 2 of) 12345 (=) 12");
		analyser.Analyse("(first 2 of) 1234567 (=) 12");
		analyser.Analyse("(first 2 of) aabbvccd (=) aa");
		analyser.Analyse("(first 2 of) aabbaavccd (=) aa");
		
		// test
		analyser.Analyse("(last 2 of) ahahaha");
		analyser.Analyse("(last 2 of) a");
		analyser.Analyse("(last 3 of) 123456789");
		analyser.Analyse("(last 7 of) 123456789");
		
		analyser.Analyse("(first 2 of) ahahaha");
		analyser.Analyse("(first 2 of) a");
		analyser.Analyse("(first 3 of) 123456789");
		analyser.Analyse("(first 7 of) 123456789");		
		//////////////////////////////////////////////////////////
	}
}
