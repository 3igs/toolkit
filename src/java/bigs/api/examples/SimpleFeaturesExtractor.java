package bigs.api.examples;

import java.util.ArrayList;
import java.util.List;

import bigs.api.core.Algorithm;
import bigs.api.core.BIGSParam;
import bigs.api.featureextraction.FeatureExtractionAlgorithm;
import bigs.api.utils.TextUtils;


public class SimpleFeaturesExtractor extends FeatureExtractionAlgorithm {

	@BIGSParam(description="lowest byte value to include")
	public Double lowPass=0.0;
	
	@BIGSParam (description="highest byte value to include")
	public Integer highPass=0;	

	@Override
	public String getDescription() {
		return "for illustration purposes";
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getClass().getSimpleName()).append(" lp=").append(TextUtils.F1.format(lowPass)).append(" hp=").append(highPass);
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
			if (b>=lowPass && b<=highPass) {
				sum = sum + b;
				count++;				
			}
		}
		if (count==0) count=1D;
		mean = sum/count;
		
		List<List<Double>> r = new ArrayList<List<Double>>();
		r.add(new ArrayList<Double>());
		r.get(0).add(mean);
		r.get(0).add(count);
		r.get(0).add(sum);
		r.get(0).add(lowPass);
		r.get(0).add(highPass.doubleValue());
		return r;
	}

	@Override
	public Integer outputDataRowkeyPrefix() {
		return Algorithm.ROWKEYPREFIX_EXPLORATION_CONFIG_STAGE;
	}
	
	
}
