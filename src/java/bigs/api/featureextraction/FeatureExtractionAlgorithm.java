package bigs.api.featureextraction;

import java.util.List;

import bigs.api.core.Algorithm;


/**
 * Feature extraction algorithms take an array of bytes (typically an image) and return a vector
 * or a matrix, represented as a List of Lists of Doubles ... in case of a vectors the top level list
 * contains one single List of Doubles. Note that this allows for matrices to have rows of 
 * different lengths.
 * 
 * @author rlx
 *
 */
public abstract class FeatureExtractionAlgorithm extends Algorithm {
	
	
	/**
	 * Refines the notion of algorithm to return a vector or matrix of doubles, 
	 * by allowing subclasses to implement the extractFeatures method.
	 * Creates a string representation of the resulting vector or matrix.
	 * This method is final, cannot be implemented by subclasses.
	 * @param source
	 * @return
	 */

	public final byte[] run (byte[] source) {
		List<List<Double>> r = this.extractFeatures(source);
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<r.size(); i++) {
			List<Double> row = r.get(i);
			for (Double d: row) {
				sb.append(d).append(" ");
			}
			if (i!=r.size()-1) {
				sb.append("; ");
			}			
		}
		
		return sb.toString().getBytes();
	}
	
	/**
	 * this method must be implemented by specific feature extraction algorithms
	 * @param source
	 * @return
	 */
	public abstract List<List<Double>> extractFeatures (byte[] source);
	
}
