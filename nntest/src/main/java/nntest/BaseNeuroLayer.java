package nntest;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nntest.compute_functions.IComputeFunction;
import nntest.data.DoubleData;
import nntest.interfaces.INeuron;

import org.apache.log4j.Logger;

import com.sec.gb.ipa.ks.common.util.AsyncCounter;

public class BaseNeuroLayer extends BaseNeuron {	
	ArrayList<INeuron> neurons = new ArrayList<INeuron>();
	public int inputNeuronsSize = 0;
	protected boolean isInputLayer = false;
	protected boolean isAutoActivation = true;
	protected boolean isMissedActivation = false;
	
	public static long[] times = new long[]{0, 0, 0}; 
	
	public BaseNeuroLayer(ArrayList<INeuron> neurons) throws Exception {
		if(neurons.contains(null) ) {
			logger.error("Input contains nulls. Can not make layer");
			throw new Exception("Input contains nulls. Can not make layer");
		}
		this.neurons = neurons;
	}
	
	protected boolean ownNeuron(INeuron neuron) {
		return neurons.contains(neuron);
	}
	
	/**
	 * Constructor for 1st layer. The layer that should just accept the data and send it to next layer
	 * @param classes
	 * @param historyLength
	 * @throws Exception 
	 */
	public BaseNeuroLayer(int classes, int historyLength) throws Exception {
		BaseNeuron neuron = new BaseNeuron(historyLength, historyLength);		
		initLayer(classes, neuron);
	}
	
	public BaseNeuroLayer(int classes, INeuron neuron ) throws Exception {
		initLayer(classes, neuron);
	}

	private void initLayer(int classes, INeuron neuron) throws Exception {
		isInputLayer = true;
		neurons = new ArrayList<INeuron>();
		inputNeuronsSize = classes;
		
		for(int ind = 0; ind < classes; ++ ind) {
			INeuron newNeuron;
			try {
				newNeuron = neuron.create();
			} catch (Exception e) {
				logger.error("Failed to create neuron. Message : " + e.getMessage());
				throw e;
			}
			newNeuron.setId(ind);
			
			neurons.add(newNeuron);
		}
	}	
	
	public boolean AddAdditionalNeurons(INeuron neuron) {
		if(neuron == null) {
			return false;
		}
		neurons.add(neuron);
		
		return true;
	}

	@Override
	public void compute() throws Exception {
		ExecutorService service = Executors.newFixedThreadPool(8);
		final AsyncCounter counter = new AsyncCounter();
		
		if( computeFunction == null ) {
			for (final INeuron neuron : neurons) {
				counter.inc();
				service.execute(new Runnable() {
					
					@Override
					public void run() {
						try {
							neuron.compute();
						} catch (Exception e) {
							logger.error("compute 1: " + e.toString());
						}		
						counter.dec();
					}
				});
				
			}		
		} else {
			// compute neuron value based on inputs, and its approximation function
			for (final INeuron neuron : neurons) {
				counter.inc();
				service.execute(new Runnable() {									
					@Override
					public void run() {
						try {
							computeFunction.compute(neuron, neuron.getId());
						} catch (Exception e) {
							logger.error("compute 2: " + e.toString());
						}
						counter.dec();
					}
				});
			}
		}
		
		counter.wait(0);
		service.shutdownNow();
	}
	
	@Override
	public void handle(INeuron inputActivation) throws Exception {
		for (INeuron neuron : neurons) {
			neuron.handle(inputActivation);
		}		
	}
	
	@Override
	public boolean addData(double data) {
		logger.error("Adding data is not supported for layer neuron");
		
		return false;
	}
	
	public boolean addData(int neuronIndex, double data) {
		if(!isInputLayer) {
			logger.error("Can not manually assign data to not input leyer");
			return false;
		}
		
		if(neuronIndex < 0 || neuronIndex >= inputNeuronsSize) {
			logger.error("Wrong neuron index : " + neuronIndex + ". Should be in range [0;" + (inputNeuronsSize - 1) + "]");
			return false;
		}
		
		neurons.get(neuronIndex).addData(new DoubleData(data));
		
		return true;
	}
	
	public boolean addData(double[] inputs) {
		if(inputs == null || inputs.length != inputNeuronsSize) {
			return false;
		}
		for(int index = 0; index < inputNeuronsSize; ++ index) {
			addData(index, inputs[index]);
		}
		
		return true;
	}
	
	@Override 
	public void activate() throws Exception {
		for (INeuron neuron : neurons) {
			try {
				neuron.activate();
			} catch (Exception e) {
				logger.error("Activating neuron : " + neuron.toString() + ", rise exception: " + e.toString());
				throw new Exception("Activating neuron : " + neuron.toString() + ", rise exception: " + e.toString());
			}
		}
		
		if(isAutoActivation) {
			finalizeActivation();
		} else {
			isMissedActivation = true;
		}
	}

	protected void finalizeActivation() throws Exception {
		if(isAutoActivation || isMissedActivation) {
			isMissedActivation = false;
			// The common practice set only other layer as out neuron.
			// It will manage everything else by self
			
			ExecutorService service = Executors.newFixedThreadPool(1);
			
			long start = new Date().getTime();
			final AsyncCounter counter = new AsyncCounter();

			for(final INeuron out: outputNeurons) {
				for (final INeuron neuron : neurons) {
					counter.inc();
					service.execute(new Runnable() {						
						@Override
						public void run() {
							try {
								out.handle(neuron);
							} catch (Throwable e) {
								logger.error("Hadnling neuron : " + out.toString() + ", rise exception: " + e.toString());
								//throw new Exception("Handling neuron : " + out.toString() + ", rise exception: " + e.toString());
							}						
							counter.dec();
						}
					});
					
				}			
			}
			
			counter.wait(0);
			
			synchronized (times) {
				times[0] += new Date().getTime() - start;
			}
			
			start = new Date().getTime();
			
			for(INeuron out: outputNeurons) {
				out.compute();
			}
			
			counter.wait(0);
			
			synchronized (times) {
				times[1] += new Date().getTime() - start;
			}
			
			for(INeuron out: outputNeurons) {
				try {
					out.activate();
				} catch (Exception e) {
					logger.error("Activating neuron : " + out.toString() + ", rise exception: " + e.toString());
					throw new Exception("Activating neuron : " + out.toString() + ", rise exception: " + e.toString());
				}
			}
			
			service.shutdownNow();
		}
	}
	
	@Override
	public void setComputeFunction(IComputeFunction computeFunction) {
		super.setComputeFunction(computeFunction);
		
		for (INeuron neuron : neurons) {
			neuron.setComputeFunction(computeFunction);
		}
	}

}
