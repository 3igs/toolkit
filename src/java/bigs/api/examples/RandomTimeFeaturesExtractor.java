package bigs.api.examples;

import java.util.ArrayList;
import java.util.List;

import bigs.api.core.Algorithm;
import bigs.api.core.BIGSParam;
import bigs.api.featureextraction.FeatureExtractionAlgorithm;
import bigs.api.utils.TextUtils;
import bigs.core.utils.Core;


public class RandomTimeFeaturesExtractor extends FeatureExtractionAlgorithm {

	@BIGSParam(description="minimum computing time (in secs)")
	public Long minTime=0L;
	
	@BIGSParam (description="maximum computing time (in secs)")
	public Long maxTime=0L;	

	@Override
	public String getDescription() {
		return "dummy computation taking a random amount of time";
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getClass().getSimpleName()).append(" minTime=").append(TextUtils.F1.format(minTime)).append(" maxTime=").append(maxTime);
		return sb.toString();
	}	
	
	/**
	 * simply returns a vector with some metrics of the input byte array (mean, count, sum, etc.)
	 */
	@Override
	public List<List<Double>> extractFeatures(byte[] source) {
		
		Long elapsedTime = minTime + new Double(Math.random()*( maxTime.doubleValue()-minTime.doubleValue())).longValue();
		Core.sleep(elapsedTime * 1000L);
		
		List<Double> v = new ArrayList<Double>();
		v.add(elapsedTime.doubleValue());
		
		List<List<Double>> r = new ArrayList<List<Double>>();
		r.add(v);
		
		return r;
	}

	@Override
	public Integer outputDataRowkeyPrefix() {
		return Algorithm.ROWKEYPREFIX_EXPLORATION_CONFIG_STAGE;
	}
	
	
}
