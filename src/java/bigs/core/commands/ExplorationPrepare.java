package bigs.core.commands;

import java.io.IOException;
import java.util.List;

import bigs.api.core.Algorithm;
import bigs.api.exceptions.BIGSException;
import bigs.api.featureextraction.FeatureExtractionAlgorithm;
import bigs.api.storage.DataSource;
import bigs.api.storage.Put;
import bigs.api.storage.Result;
import bigs.api.storage.ResultScanner;
import bigs.api.storage.Scan;
import bigs.api.storage.Table;
import bigs.core.BIGS;
import bigs.core.explorations.Evaluation;
import bigs.core.explorations.Exploration;
import bigs.core.explorations.ExplorationStage;
import bigs.core.utils.Data;
import bigs.core.utils.Log;
import bigs.core.utils.Text;


public class ExplorationPrepare extends Command {

	@Override
	public String getCommandName() {		
		return "exploration.prepare";
	}

	@Override
	public String[] getHelpString() {
		return new String[]{"'bigs "+this.getCommandName()+" explorationNumber'"};
	}

    @Override
    public Boolean checkCallingSyntax(String[] args) {
    	if (args.length!=1) return false;
		try {
			Long l = new Long(args[0]);
		} catch (NumberFormatException e) {
			return false;
		}
    	return true;
    }

    @Override
	public void run(String[] args) {
		
		Long explorationNumber = new Long(args[0]);
		Exploration expl = Exploration.fromExplorationNumber(explorationNumber);

		
		List<ExplorationStage> stages = expl.getStages();
		if (stages.size()==0) {
			throw new BIGSException("no stages defined in exploratin file");
		}
		Log.info("Processing only stage one");
		ExplorationStage stage = stages.get(0);
		
    	
    	// creates the exploration configurations, repeats, etc.
		stage.saveEvaluations();
		Log.info("Exploration "+explorationNumber+" generated and saved "+stage.generateEvaluations().size()+" evaluations ");
		
    	// marks the source dataset for splitting
    	Data.markSplits(stage.getConfiguredOriginDataSource(), stage.getOriginContainerName(), stage.getNumberOfSplits(), Text.zeroPad(explorationNumber));
    	Log.info("marked data splits in source dataset");
    	
    	expl.setStatus(Exploration.STATUS_ACTIVE);
    	expl.setTimeDone(null);
    	expl.setTimeStart(null);
    	expl.save();
    	Log.info("exploration marked as active");
	}

	@Override
	public String getDescription() {
		return "creates all evaluations for an exploration, leaving them ready for workers";
	}

}
