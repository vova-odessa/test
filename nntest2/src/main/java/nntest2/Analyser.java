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
import nntest2.data.NeuronData;
import nntest2.data.StringData;
import nntest2.data.TextData;
import nntest2.herpers.NeuroHelper;
import nntest2.neurons.Neuron;

public class Analyser {
	Logger logger = Logger.getLogger(Analyser.class);
	
	private class AnalysisResult {
		public TextData originalRequest = null;
		public Neuron operatorNeuron = null;
		public ArrayList<Data> parameters = null;
		public Data result = null;
	}
	
	public static final StringData EQ_OPER = new StringData("(=)");
	public static final StringData NOT_EQ_OPER = new StringData("(!=)");
	public static final StringData THEN_OPER = new StringData("(=>)");
	public static final StringData THEN_ONLY_THEN_OPER = new StringData("(<=>)");
	
	private final Set<StringData> defaultOperators = new HashSet<>(Arrays.asList(new StringData[] {EQ_OPER, THEN_OPER, THEN_ONLY_THEN_OPER, NOT_EQ_OPER}));
	private Set<StringData> extendedOperators = new HashSet<>();
	//private HashMap<Data, Data> labels = new HashMap<>();
	private Queue<Fact> prevFacts = new LinkedList<>();
	private int maxMemorySize = 100;
	
	public Data Analyse(Fact fact) {
		Fact left = extractFactEx(fact.first.toString());
		
		ArrayList<Data> inputList = new ArrayList<>();
		if(left.operator.toString().length() > 0) {
			// try to learn it
			if(left.first.toString().trim().length() > 0) {
				if(left.first instanceof TextData || left.first instanceof ArrayData) {
					inputList.addAll(Arrays.asList(left.first.elements()));
				} else {
					inputList.add(left.first);
				}
			}
			if(left.second.toString().trim().length() > 0) {
				if(left.second instanceof TextData || left.second instanceof ArrayData) {
					inputList.addAll(Arrays.asList(left.second.elements()));
				} else {
					inputList.add(left.second);
				}
			}
		}
		
		if(fact.operator != null && fact.operator.compareTo(EQ_OPER) == 0) {
			if(inputList.size() > 0) {
				// has training operator, train data
				Neuron neuron = NeuroBase.getInstance().validateNeuron(left.operator);
				neuron.train(fact.operator.toNeuronData(), inputList, fact.second);
				
				logger.info(neuron.toString());
				
				Data crossValidationData = neuron.computeEx(fact.operator.toNeuronData(), inputList);
				
				if(crossValidationData == null || crossValidationData.compareTo(fact.second) != 0) {
					logger.info("Cross validation failed. Need more data");
				} else {
					logger.info("Cross validation success. Neuron trained");
				}
			} else {
				// make association
			}
		} else if(fact.operator == null || fact.operator.elements().length == 0) {
			// no global training operator, consider as request
			if(inputList.size() > 0) {
				Neuron neuron = NeuroBase.getInstance().findNeuron(left.operator);
				if(neuron != null) {
					return neuron.compute(new NeuronData(left.operator.toString()), inputList);
				} else {
					logger.error("Do not know how to perform (" + left.operator + ") please provide example" );
				}
			}
		}
		
		return null;
	}
	
	public void Analyse(String text) {
		logger.info("Q: " + text);
		
		TextData data = new TextData(Data.construct(text).elements());
		//TextData data = new TextData(text);
		Data result = NeuroBase.getInstance().analyseText(data);
		
		if(result == null) {		
			/*Fact fact = extractBaseFact(text);
			
			if(fact != null) {
				result = Analyse(fact);
			}*/
			result = NeuroHelper.compute.compute(null, data);
			// TODO consider further analysis
			logger.info("Computation managed by simple analysis for training.");
		} else {
			logger.info("Computation managed by neuron base.");
		}
		
		if(result != null) {
			logger.info("Q[" + text + "] A = [" + result + "]");
		} else {
			logger.info("Q[" + text + "] -> no answer");
		}
		
		logger.info("");
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
			elements[0] = Data.construct(text.substring(0, openBrIndex).trim());
			elements[1] = Data.construct(text.substring(openBrIndex + 1, closeBrIndex).trim());
			elements[2] = Data.construct(text.substring(closeBrIndex + 1).trim());
		}
		
		if(elements == null) {
			elements = new Data[]{Data.construct(text)};
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
		// not
		analyser.Analyse("(not) true (=) false");
		analyser.Analyse("(not) false (=) true");
		
		// last N
		analyser.Analyse("(last 2 of) 12345 (=) 45");
		analyser.Analyse("(last 2 of) 1234567 (=) 67");
		analyser.Analyse("(last 2 of) aabbvccd (=) cd");
		analyser.Analyse("(last 2 of) aabbaavccd (=) cd");
		analyser.Analyse("(last 2 of) bb (=) bb");		
		
		// starts with
		analyser.Analyse("(starts with) 89878952 98 (=) false");
		analyser.Analyse("(starts with) 8987fdfdf2 878 (=) false");
		analyser.Analyse("(starts with) hdjkl jk (=) false");
		analyser.Analyse("(starts with) jdfdfklh kl (=) false");
		analyser.Analyse("(starts with) fdjkjjjkjk 45454 (=) false");
		analyser.Analyse("(starts with) 12345678 123 (=) true");
		analyser.Analyse("(starts with) 1234 12 (=) true");
		analyser.Analyse("(starts with) aaa aaa (=) true");
		analyser.Analyse("(starts with) aabbaavccd aab (=) true");
		analyser.Analyse("(starts with) hahahah ha (=) true");
		analyser.Analyse("(starts with) pppoooo pppo (=) true");
		analyser.Analyse("(starts with) pppoooo uuu (=) false");
		
		// first N
		analyser.Analyse("(first 2 of) 12345 (=) 12");
		analyser.Analyse("(first 2 of) 1234567 (=) 12");
		analyser.Analyse("(first 2 of) aabbvccd (=) aa");
		analyser.Analyse("(first 2 of) aabbaavccd (=) aa");
		
		// eq
		analyser.Analyse("a (equals) a (=) true");
		analyser.Analyse("b (equals) b (=) true");
		analyser.Analyse("22 (equals) 22 (=) true");
		analyser.Analyse("p (equals) b (=) false");
		analyser.Analyse("w (equals) wx (=) false");
		analyser.Analyse("ghf (equals) gh (=) false");
		analyser.Analyse("xyz (equals) xyw (=) false");
		analyser.Analyse("klm (equals) klmn (=) false");
		analyser.Analyse("habr (equals) hab (=) false");
		
		// in
		analyser.Analyse("a (in) {a, b, c} (=) true");
		analyser.Analyse("b (in) {d, f} (=) false");
		analyser.Analyse("ab (in) {ab, f} (=) true");
		analyser.Analyse("123 (in) {ab, fd, 1, 5} (=) false");
		
		analyser.Analyse("{a, b, c} (has) a (=) true");
		analyser.Analyse("{d, f} (has) b(=) false");
		analyser.Analyse("(in) {ab, f} (has) ab(=) true");
		analyser.Analyse("{ab, fd, 1, 5} (has) 123 (=) false");
		
		//////////////////////////////////////////////////////////////////
		// test
		analyser.Analyse("(last 2 of) ahahaha");
		analyser.Analyse("(last 2 of) a");
		analyser.Analyse("(last 3 of) 123456789");
		analyser.Analyse("(last 7 of) 123456789");
		
		analyser.Analyse("abracadabra (starts with) abra");
		analyser.Analyse("abracadabra (starts with) cadabra");
		
		analyser.Analyse("(first 2 of) ahahaha");
		analyser.Analyse("(first 2 of) a");
		analyser.Analyse("(first 3 of) 123456789");
		analyser.Analyse("(first 7 of) 123456789");	
		
		analyser.Analyse("34 (equals) 34");
		analyser.Analyse("34 (equals) 35");
		analyser.Analyse("abc (equals) abc");
		analyser.Analyse("abcd (equals) abc");
		
		analyser.Analyse("34 (in) {5, 10, 15}");
		analyser.Analyse("a (in) {2, 8, a, 1}");
		
		analyser.Analyse("{5, 10, 15} (has) 34");
		analyser.Analyse("{2, 8, a, 1} (has) a");
		//////////////////////////////////////////////////////////
	}
}
