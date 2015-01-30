package nntest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

import nntest.compute_functions.IComputeFunction;
import nntest.data.Data;
import nntest.data.DoubleData;
import nntest.interfaces.INeuron;

import org.apache.log4j.Logger;

public class BaseNeuron extends INeuron{
	private static final DoubleData DEFAULT_W = new DoubleData(0.50);

	protected Logger logger = Logger.getLogger(BaseNeuron.class);
	
	private int id = genId();
	protected int neuronStateMemorySize = 1;
	protected int inputDataPerNeuronNumber = 1;
	public LinkedList<Data> state = new LinkedList<Data>();
	
	protected IComputeFunction computeFunction = null;
	
	private HashSet<INeuron> inputNeurons = new HashSet<INeuron>();
	protected HashSet<INeuron> outputNeurons = new HashSet<INeuron>();
	
	protected TreeMap<Integer, Data[] > data = new TreeMap<Integer, Data[]>();
	protected TreeMap<Integer, double[] > W = new TreeMap<Integer, double[]>();
	public ArrayList<INeuron> classes = null;
	private double tetta = 0.0;
	protected int numClasses = 1;
	
	protected BaseNeuron() {
		//state.add(new DoubleData(0.0));
	}
	
	public TreeMap<Integer, Data[] > getData() {
		return data;
	}
	
	public TreeMap<Integer, double[] > getW() {
		return W;
	}
	
	public int getNumClasses() {
		return numClasses;
	}
	
	public ArrayList<INeuron> getClasses() {
		return classes;
	}
	
	static ArrayList<Integer> lastId = new ArrayList<>();
	public static int genId() {
		synchronized (lastId) {
			if(lastId.size() == 0) {
				lastId.add(0);
			}
			lastId.set(0, lastId.get(0) + 1);
			return lastId.get(0);
		}
	}
	
	public void allocateClasses(int numClasses) {
		this.numClasses = numClasses;
		classes = new ArrayList<>();
		
		for(int i = 0; i < numClasses; ++ i) {
			classes.add(new BaseNeuron());
		}
	}
	
	protected BaseNeuron(int neuronStateMemory, int inputMemoryNumber) throws Exception {
		if(neuronStateMemory < 1) {
			throw new Exception("data number = " + neuronStateMemory + " < 1 is invalid");
		}
		
		if(inputMemoryNumber < 1) {
			throw new Exception("input per neuron number = " + neuronStateMemory + " < 1 is invalid");
		}
				
		this.inputDataPerNeuronNumber = inputMemoryNumber;
		
		setNeuronStateMemorySize(neuronStateMemory);
	}

	protected void setNeuronStateMemorySize(int dataNumber) {
		this.neuronStateMemorySize = dataNumber;	
		
		state.clear();
		
		while (state.size() < dataNumber) {
			state.add(new DoubleData(0.0));
		}
	}
	
	public Set<INeuron> getInputNeurons() {
		return inputNeurons;
	}
	
	public boolean registerInput(INeuron input) {
		// NOTE: below is not mandatory
		//if(this.inputDataPerNeuronNumber != input.dataNumber) {
		//	logger.error("Neurons not compatible. Input required = " + inputDataPerNeuronNumber + ", but actual is " + input.dataNumber);
		//	return false;
		//}
		
		inputNeurons.add(input);
		input.registerOutput(this);
		
		return true;
	}
	
	public boolean registerOutput(INeuron output) {
		if(output == null) {
			return false;
		}
		
		outputNeurons.add(output);
		return true;
	}
	
	private Integer w = new Integer(0);
	public void handle(INeuron inputActivation) throws Exception {
		synchronized (w) 
		{
			if(getId() == inputActivation.getId()) {
				//logger.error("same object");
			}
			handleForData(inputActivation, inputActivation.getState());					
		}		
	}

	protected void handleForData(INeuron inputActivation, ArrayList<Data> inpState) {
		Data [] input = new Data[inpState.size()];
		//int index = input.length - 1;
		int index = 0;
		
		if(!inputNeurons.contains(inputActivation)) {
			inputNeurons.add(inputActivation);
		}		
		
		for(Data inp: inpState) {
			input[index++] = inp;
		}	
					
		data.put(inputActivation.getId(), input);			
			
		if(!W.containsKey(inputActivation.getId()) ) {
			
			double [] iw = new double[inpState.size()];
			for(int ind = 0; ind < iw.length; ++ ind) {
				iw[ind] = DEFAULT_W.doubleValue();
			}
		
			W.put(inputActivation.getId(), iw);
		}
	}
	
	public void compute() throws Exception {
		if(computeFunction != null) {
			computeFunction.compute(this);
		} else {
			// make just sum of inputs
			double val = sumInputs();			
			addData(val);
		}
	}

	public double sumInputs() {
		double val = 0;
		for(Entry<Integer, Data[]> input: data.entrySet()) {
			Data[] x = input.getValue();
			/*double[] w = W.get(input.getKey());
			
			for(int j = 0; j < neuronStateMemorySize; ++ j) {
				val += x[j]*w[j];
			}*/
			val += computeInput(input.getKey(), x, true);
		}
		val += getTetta();
		return val;
	}
	
	public double computeInput(Integer key, Data[] x, boolean includeHistory) {
		int last = 1;
		if(includeHistory) {
			last = neuronStateMemorySize;
		}
		
		double val = 0;
		double[] w = W.get(key);
		for(int j = 0; j < last; ++ j) {			
			if(w != null) {
				if(x.length <= j || w.length <= j) {
					logger.error("computeInput: j out of bounds of x.size = " + x.length + ", w.size = " + w.length);
					return val;
				}
				val += x[j].doubleValue()*w[j];
			} else if(x[j].isSameType(BaseNeuron.class)) {
				BaseNeuron input = (BaseNeuron)x[j].getData();
				
				if(!W.containsKey(input.getId())) {
					logger.error("No data for input [key = " + key + "] history = " + j);
				} else {
					val += W.get(input.getId())[j];
				}
			} else {
				logger.error("No data for input [key = " + key + "] history = " + j);
			} 
		}
		
		return val;
	}
	
	public Data train(Data expected) {
		if(computeFunction != null) {
			boolean trainState = isLocked();
			//if(! isLocked()) {
				//lock(true);
				computeFunction.train(this, expected.doubleValue());
				//lock(false);
			//}
			return getLastOutput();
		} else {			
			return getLastOutput();
		}
	}
	
	public boolean supportTraining() {
		return computeFunction != null && !isLocked();
	}
		
	public void activate() throws Exception {
		for(INeuron out: outputNeurons) {
			try {
				out.handle(this);
			} catch (Exception e) {
				logger.error("sending output to neuron: " + out.toString() + "rise exception. " + e.toString());
				throw new Exception("sending output to neuron: " + out.toString() + ", rise exception: " + e.toString());
			}
		}
	}
	
	public boolean addData(double data) {
		state.addFirst(new DoubleData(data));
		state.removeLast();
		
		return true;
	}
	
	public boolean changeData(double data) {
		state.getFirst().set(data);
		
		try {
			activate();
		} catch (Exception e) {
		}
		
		return true;
	}
	
	public Data getLastOutput() {		
		return state.getFirst();
	}

	/**
	 * @param computeFunction the computeFunction to set
	 */
	public void setComputeFunction(IComputeFunction computeFunction) {
		this.computeFunction = computeFunction;
	}

	/**
	 * @return the tetta
	 */
	public double getTetta() {
		return tetta;
	}

	/**
	 * @param tetta the tetta to set
	 */
	public void setTetta(double tetta) {
		this.tetta = tetta;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public ArrayList<Data> getState() {
		return new ArrayList<>(state);
	}

	@Override
	public int compareTo(INeuron o) {
		if(o == null) {
			return 1;
		}
		return new Integer(id).compareTo(new Integer(o.getId()));
	}

	@Override
	public boolean addData(Data data) {
		return addData(data.doubleValue());
	}

	@Override
	public boolean changeData(Data data) {
		return changeData(data.doubleValue());
	}

	@Override
	public INeuron create() {
		try {
			return new BaseNeuron(state.size(), inputDataPerNeuronNumber);
		} catch (Exception e) {
			logger.warn("Creation was not success with same attrivutes.");
		}
		
		return new BaseNeuron();
	}

	@Override
	public void trainKid(INeuron kid, Data expected) {
		if(kid.supportTraining()) {
			kid.train(expected);
		}
	}

	@Override
	public void clearInputs() {
		if(!isLocked()) {
			lock(true);
			
			for (INeuron neuron : inputNeurons) {
				neuron.clearInputs();
			}
			
			for(int i = 0; i < state.size(); ++i) {
				state.set(i, new DoubleData(0));
			}
		}
	}

	@Override
	public void lockAll(boolean state) {
		if(state == true && isLocked()) {
			return;
		}
		
		lock(state);
		
		for(INeuron input: inputNeurons) {
			input.lock(state);
		}
	}
}
