package bigs.core.explorations;

import java.util.ArrayList;
import java.util.List;

import bigs.api.core.Algorithm;
import bigs.api.core.BIGSParam;
import bigs.api.exceptions.BIGSException;
import bigs.api.storage.DataSource;
import bigs.api.utils.TextUtils;
import bigs.core.exceptions.BIGSPropertyNotFoundException;
import bigs.core.utils.Core;
import bigs.core.utils.Log;
import bigs.core.utils.Text;


public class ExplorationStage {

	static String lengine         = "engine";
	static String lnumberOfSplits = "nbsplits";
	static String lsourceTable    = "sourcetable";
	
	Exploration exploration;
	String stage = "";
	String prefix = "";
	
	Integer numberOfSplits = 1;
		
	public ExplorationStage(Exploration exploration, String stage) {
		this.exploration = exploration;
		this.stage = stage;
		this.prefix = Exploration.lstage+"."+stage;
		
		try {
			this.numberOfSplits = Text.parseObject(exploration.getProperty(prefix+"."+lnumberOfSplits), Integer.class);
		} catch (Exception e) {
			throw new BIGSException("could not get "+lnumberOfSplits+" from stage definition. "+e.getMessage());
		}
		
	}
	
	public String toString() {
		return stage;
	}
	
	public Integer getNumberOfSplits() {
		return numberOfSplits;
	}
	
	public void setNumberOfSplits(Integer numberOfSplits) {
		this.numberOfSplits = numberOfSplits;
	}
	
	public Long getStageNumber() {
		return new Long(stage);
	}
	
	public String getOriginContainerName() {
		String r = exploration.getProperty(prefix+".origin.table");
		if (r==null) throw new BIGSException ("no origin table name defined in stage "+stage);
		return r;
	}

	public String getDestinationContainerName() {
		String r= exploration.getProperty(prefix+".destination.table");
		if (r==null) throw new BIGSException ("no destination table name defined in stage "+stage);
		return r;
	}

	public DataSource getConfiguredOriginDataSource() {
		DataSource r;
		try {
			r = Core.getConfiguredObject(
					"datasource", DataSource.class, exploration, prefix+".origin");
		} catch (BIGSPropertyNotFoundException e) {
			throw new BIGSException("error in properties: "+e.getMessage());
		}
		
		r.initialize();
		return r;
	}
	
	public DataSource getConfiguredDestinationDataSource() {
		DataSource r;
		try {
			r = Core.getConfiguredObject(
					"datasource", DataSource.class, exploration, prefix+".origin");
		} catch (BIGSPropertyNotFoundException e) {
			throw new BIGSException("error in properties: "+e.getMessage());
		}		
		r.initialize();
		return r;
	}

	public List<Algorithm> getConfiguredAlgorithmList() {
		List<Algorithm> r = 
				Core.getConfiguredObjectList(
						lengine, Algorithm.class, exploration, prefix);
		return r;				
	}
	
	public void saveEvaluations() {
    	for (Evaluation eval: this.generateEvaluations()) {
    		eval.save();
    	}		
	}
	
	public List<Evaluation> generateEvaluations() {

			List<Evaluation> r = new ArrayList<Evaluation>();
		
	    	// TODO: for the moment for feature extraction algorithms the number of repeats and runs stays at one
	    	Long nbRepeats = 1L;
	    	Long nbRuns    = 1L;

	    	// initialize configuration counter
	    	Long confNumber=1L;
	    	
	    	Evaluation evalExploration = new Evaluation();
	    	evalExploration.setExplorationNumber(this.exploration.getExplorationNumber());
	    	r.add(evalExploration);
	    	
	    	for (Long repeatNumber=1L; repeatNumber<=nbRepeats; repeatNumber++) {
	    		Evaluation evalRepeat = evalExploration.clone();
	    		evalRepeat.setRepeatNumber(repeatNumber);
	    		r.add(evalRepeat);

				for (Algorithm f: this.getConfiguredAlgorithmList()) {
					
					Evaluation evalConfig = evalRepeat.clone();
					
		    		evalConfig.setConfigNumber(confNumber);
					evalConfig.setConfiguredAlgorithm(f);						
					r.add(evalConfig);

		        	// TODO: need to loop somehow over the stages, for the moment simply store current stage
					Evaluation evalStage = evalConfig.clone();
		        	evalStage.setStageNumber(this.getStageNumber());
		        	r.add(evalStage);

		        	for (Long runNumber=1L; runNumber<=nbRuns; runNumber ++ ) {
		        		Evaluation evalRun = evalStage.clone();
			    		evalRun.setRunNumber(runNumber);
			    		r.add(evalRun);

			        	for (Long splitNumber=1L; splitNumber<=this.getNumberOfSplits(); splitNumber++) {
			        		Evaluation evalSplit = evalRun.clone();
							evalSplit.setSplitNumber(splitNumber);
							r.add(evalSplit);					    	
						}
						confNumber++;
					}
				}
			}
	    	return r;
	  
	    }	
}
