package bigs.api.examples;

import java.util.ArrayList;
import java.util.List;

import bigs.api.core.Algorithm;
import bigs.api.featureextraction.FeatureExtractionAlgorithm;


public class NoParamsFeaturesExtractor extends FeatureExtractionAlgorithm {

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getClass().getSimpleName());
		return sb.toString();
	}	
	
	/**
	 * simply returns a vector with some metrics of the input byte array (mean, count, sum, etc.)
	 */
	@Override
	public List<List<Double>> extractFeatures(byte[] source) {
		
		Double mean = 0D;
		Double count = 0D;
		Double sum = 0D;
		
		for (byte b: source) {
			sum = sum + b;
			count++;				
		}
		if (count==0) count=1D;
		mean = sum/count;
		
		List<List<Double>> r = new ArrayList<List<Double>>();
		r.add(new ArrayList<Double>());
		r.get(0).add(mean);
		r.get(0).add(count);
		r.get(0).add(sum);
		return r;
	}

	@Override
	public Integer outputDataRowkeyPrefix() {
		return Algorithm.ROWKEYPREFIX_EXPLORATION_CONFIG_STAGE;
	}
	
	
}