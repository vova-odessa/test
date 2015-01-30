package nntest.data;

public class FuzzyValue {
	private double signal = 0.0;
	private double experience = 0.01;
	
	public double trainExpected(double signal) {
		return trainExpected(signal, 1.0);
	}
	
	public double trainExpected(double expectedSignal, double confidence) {
		signal = (signal * experience + expectedSignal * confidence)/(experience + confidence);
		experience += confidence;
		return signal;
	}
	
	public double trainObservation(double signal) {
		return trainObservation(signal, 1.0);
	}
	
	public double trainObservation(double expectedSignal, double confidence) {
		//signal = (signal * experience + expectedSignal * confidence)/(experience + confidence);
		if(signal < expectedSignal) {
			trainExpected(1.0, confidence);
		} else if(signal > expectedSignal) {
			trainExpected(-1.0, confidence);
		}
		//signal = expectedSignal;
		
		experience += confidence;
		return signal;
	}
	
	public double getSignal() {
		return signal;
	}
}
