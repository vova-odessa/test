package nntest;

import java.util.TreeMap;

import org.apache.log4j.Logger;

public class Util {
	private static Logger logger = Logger.getLogger(Util.class);
	
	public static double sum(double[] vec) {
		double sum = 0.0;
		for (double d : vec) {
			sum += d;
		}
		
		return sum;
	}
	
	public static double sum(TreeMap<Integer, double[]> matr) {
		double sum = 0.0;
		
		for (double[] row : matr.values()) {
			sum += sum(row);
		}
		
		return sum;
	}
	
	public static Double[] makeBitArray(int size, int pos1) {
		if(size <= 0) {
			logger.error("Params are invalid");
			return null;
		}
		
		Double[] res = new Double[size];
		
		for(int i = 0; i < size; ++ i) {
			if(i == pos1) {
				res[i] = new Double(1.0);
			} else {
				res[i] = new Double(0.0);
			}
		}
		
		return res;
	}
	
}
