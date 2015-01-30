package nntest.interfaces;

import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import com.sec.gb.ipa.ks.common.util.AsyncCounter;

import nntest.compute_functions.IComputeFunction;
import nntest.data.Data;

public abstract class INeuron implements Comparable<INeuron>{
	private AsyncCounter isTrain = new AsyncCounter();
	
	
	public abstract boolean registerInput(INeuron input);
	public abstract boolean registerOutput(INeuron output);
	public abstract void handle(INeuron inputActivation) throws Exception;
	public abstract void compute() throws Exception;
	public abstract void activate() throws Exception;
	public abstract int getId();
	public abstract void setId(int id);
	abstract public Data getLastOutput();
	abstract public Data train(Data expected);
	abstract public void trainKid(INeuron kid, Data expected);
	public abstract boolean supportTraining();
	public abstract ArrayList<Data> getState();
	public abstract boolean addData(Data data);
	public abstract boolean changeData(Data data);
	public abstract void clearInputs();
	
	public abstract void lockAll(boolean state);
	
	public void setComputeFunction(IComputeFunction computeFunction) {
	}
	
	public abstract INeuron create();
	public double sumInputs() {
		return 0;
	}
	public double getTetta() {
		return 0;
	}
	public void setTetta(double d) {
	}
	public abstract Set<INeuron> getInputNeurons();
	
	public void lock(boolean state) {
		synchronized (isTrain) {
			if(state) {
				if(isTrain.get() == 0) {
					isTrain.inc();
				}
			} else {
				if(isTrain.get() == 1) {
					isTrain.dec();
				}
			}
		}
	}
		
	public boolean isLocked() {
		return !isTrain.isEmpty();
	}
}
