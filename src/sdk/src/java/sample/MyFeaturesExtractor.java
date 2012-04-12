package sample;

import java.util.ArrayList;
import java.util.List;

import bigs.api.core.Algorithm;
import bigs.api.core.BIGSParam;
import bigs.api.featureextraction.FeatureExtractionAlgorithm;
import bigs.api.utils.TextUtils;

public class MyFeaturesExtractor extends FeatureExtractionAlgorithm {

	@BIGSParam
	public Double myParam1=0.0;
	
	@BIGSParam 
	public Integer myParam2=0;	

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getClass().getSimpleName()).append(" p1=").append(TextUtils.F1.format(myParam1)).append(" p2=").append(myParam2);
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
			count++;
			sum = sum + b;
		}
		mean = sum/count;
		
		List<List<Double>> r = new ArrayList<List<Double>>();
		r.add(new ArrayList<Double>());
		r.get(0).add(mean);
		r.get(0).add(count);
		r.get(0).add(sum);
		r.get(0).add(myParam1);
		r.get(0).add(myParam2.doubleValue());

		return r;
	}

	@Override
	public Integer outputDataRowkeyPrefix() {
		return Algorithm.ROWKEYPREFIX_EXPLORATION_CONFIG_STAGE;
	}
	
	
}
