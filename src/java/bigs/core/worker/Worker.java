package bigs.core.worker;

import java.util.Date;
import java.util.List;

import bigs.api.core.Algorithm;
import bigs.api.storage.DataSource;
import bigs.api.storage.Put;
import bigs.api.storage.Result;
import bigs.api.storage.ResultScanner;
import bigs.api.storage.Scan;
import bigs.api.storage.Table;
import bigs.core.BIGS;
import bigs.core.BIGSProperties;
import bigs.core.explorations.Evaluation;
import bigs.core.explorations.Exploration;
import bigs.core.explorations.ExplorationStage;
import bigs.core.utils.Core;
import bigs.core.utils.Data;
import bigs.core.utils.Log;
import bigs.core.utils.Text;


public class Worker {
	
	public Evaluation currentEvaluation = null;
	
	Boolean abort = false;
	
	/**
	 * returns true if the eval passed as argument is available to start working on it.
	 * Basically considers evals PENDING and evals INPROGRESS with no recent ping
	 */
	boolean needsProcessing(Evaluation eval) {
		if (eval.isStatusPending()) return true;		

		if (eval.isStatusInProgress()) {
			Long now = Core.getTime();
			Long lastUpdate = eval.getLastUpdate().getTime();
			
			if (now-lastUpdate>BIGSProperties.WORKER_CLEAN_INTERVAL) {
				Log.debug("cleaning up "+eval.getRowKey());
				eval.setStatus(Evaluation.STATUS_PENDING);
				eval.save();
				return true;
			}
		}
		return false;
	}
	
	/**
	 * returns true if the evaluation passed as argument is the first 
	 * evaluation to process in this explorations. This is used to mark
	 * the start time of the exploration
	 * @param eval
	 * @return 
	 */
	boolean isFirstEvalInExploration(Evaluation eval) {
		if (eval.getRepeatNumber()==1 && eval.getConfigNumber()==1 && eval.getSplitNumber()==1) {
			return true;
		} else {
			return false;
		}
	}
	
	
	Evaluation selectNextEvaluation() {
		
		// retrieve the explorations which are active in the DB
    	List<Exploration> activeExplorations = Exploration.getExplorationsForStatus(Exploration.STATUS_ACTIVE);
    	if (activeExplorations == null || activeExplorations.size()==0) {
    		return null;
    	}
    	
    	// Look what exploration still has evaluations pending
    	for (Exploration expl: activeExplorations) {
    		List<Evaluation> evals = expl.getAllEvaluations(Core.NOCACHE);
    		
    		// check if all splits are done and mark the exploration done if so
    		Boolean anySplitNotDone = false;
    		for (Evaluation eval: evals) {
    			if (eval.isSplit() && !eval.isStatusDone()) anySplitNotDone = true;    			
    		}
    		
    		// if the exploration is active and all evaluations are done, mark the exploration as done
    		if (!anySplitNotDone) {
    			expl.setStatus(Exploration.STATUS_DONE);
    			expl.setTimeDoneFromTimeReference();
    			expl.save();
    			continue;
    		}

    		// Now select which evaluation within the exploration
    		for (Evaluation eval: evals) {
    			eval.setParentExploration(expl);
    			if (!eval.isSplit()) continue;
    			if (!needsProcessing(eval)) continue;												
    			return eval;
    		}
    		
    		// if we got here no evaluation was selected and we move over next exploration
    	}
    	return null;
		
	}
	
	/**
	 * starts this worker in a continuous loop checking regularly for evaluations to do
	 */
	public void start() {
		Log.info("starting worker "+this.toString());
        AliveThread athread = new AliveThread(this);
        athread.start();

        Log.info("[time alive "+Text.timeToString(BIGSProperties.WORKER_ALIVE_INTERVAL)+"] ");
        Log.info("[time sleep "+Text.timeToString(BIGSProperties.WORKER_SLEEP_INTERVAL)+"] ");
        Log.info("[time clean "+Text.timeToString(BIGSProperties.WORKER_CLEAN_INTERVAL)+"] ");
        
        while (true) {

        	try {
	        	Evaluation eval = this.selectNextEvaluation();
	    		if (eval!=null) {
					// set status to INPROGRESS and take over the split
					Table evalTable = BIGS.globalProperties.getConfiguredDataSource().getTable(Evaluation.tableName);
					Put put = evalTable.createPutObject(eval.getRowKey());
					eval.setStatus(Evaluation.STATUS_INPROGRESS);
					put = eval.fillPutObject(put);
					put = Data.fillInHostMetadata(put);
					Boolean success = evalTable.checkAndPut(eval.getRowKey(), "bigs", "status", Evaluation.getStatusString(Evaluation.STATUS_PENDING).getBytes(), put);
					// if we did not succeed in setting the status to pending it is because somebody else did and it is working on it
					if (success) {
						if (this.isFirstEvalInExploration(eval)) {
							eval.getParentExploration().setTimeStartFromTimeReference();
							eval.getParentExploration().setTimeDone(null);
							eval.getParentExploration().save();
						}
						this.doEval(eval);
						if (!abort) {
							eval.setStatus(Evaluation.STATUS_DONE);
							eval.save();
						} else {
							Log.info("evaluation aborted");
							abort = false;
						}
	        		}        			
	        	} else {
	            	Log.info("no active explorations found. will look again in a while");
	            	Core.sleep(BIGSProperties.WORKER_SLEEP_INTERVAL);
	        	}        	
        	} catch (Exception e) {
        		Log.error("worker catched exception "+e.getClass().getName()+", "+e.getMessage());
        		Log.error("will wait a while and start looking to something to do again");
        		Core.sleep(BIGSProperties.WORKER_ALIVE_INTERVAL);
        	}
        }
	}


	
	public void doEval(Evaluation eval) {
		
		Log.info("worker on eval "+eval.toString());
		currentEvaluation = eval;

		Integer stageNumber = eval.getStageNumber().intValue()-1;
		ExplorationStage stage = eval.getParentExploration().getStages().get(stageNumber);
		DataSource originDataSource    = stage.getConfiguredOriginDataSource();
		String     originDataTableName = stage.getOriginContainerName();
		DataSource destinationDataSource    = stage.getConfiguredDestinationDataSource();
		String     destinationDataTableName = stage.getDestinationContainerName();
				
		Algorithm algorithm = eval.getConfiguredAlgorithm();
		
		Data.createDataTableIfDoesNotExist(destinationDataSource, destinationDataTableName);

		Table originTable = originDataSource.getTable(originDataTableName);
		Table destinationTable = destinationDataSource.getTable(destinationDataTableName);
				
		Scan scan = originTable.createScanObject();
		for (String family: Data.dataTableColumnFamilies) {
			scan.addFamily(family);
		}
		scan.setFilterByColumnValue("splits", Text.zeroPad(eval.getExplorationNumber()), eval.getSplitNumber().toString().getBytes());
		ResultScanner rs = originTable.getScan(scan);
		try {
			for (Result rr = rs.next(); rr!=null; rr = rs.next()) {						
				Log.info("      input  rowkey "+rr.getRowKey());

				byte[] bytes = rr.getValue("content", "data");

				Date startTime = new Date();
				//---------------------------------------
				// This is the actual algorithm running
				byte[] result = algorithm.run(bytes);
				//---------------------------------------
				if (abort) {
					Log.error("aborting this evaluation: "+eval.getRowKey());
					return;
				}
				Date endTime = new Date();
				
				eval.addToElapsedTime(endTime.getTime() - startTime.getTime());

				String destinationRowKey = eval.getRowKey()+":"+rr.getRowKey();
				if (algorithm.outputDataRowkeyPrefix()==Algorithm.ROWKEYPREFIX_EXPLORATION_CONFIG_STAGE) {
					destinationRowKey = Text.zeroPad(eval.getExplorationNumber())+"."+
				                        Text.zeroPad(eval.getConfigNumber())+"."+
				                        Text.zeroPad(eval.getStageNumber())+":"+
				                        rr.getRowKey();					
				}				
				Log.info("      output rowkey "+destinationRowKey);
				
				Put put = destinationTable.createPutObject(destinationRowKey);
				put.add("content", "data", result);
				put = Data.fillInHostMetadata(put);
				destinationTable.put(put);
			}
		} finally {
			rs.close();
		}		
		currentEvaluation = null;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("worker, uuid=").append(Core.myUUID);
		return sb.toString();
	}
	
	
	class AliveThread extends Thread {
        Worker worker;  
        Boolean stop = false;
        
        AliveThread (Worker w) {
                worker = w;
        }
        
        @Override
        public void run() {        	
                while (!stop) {
                        Core.sleep(BIGSProperties.WORKER_ALIVE_INTERVAL);
                        DataSource bigsDataSource = BIGS.globalProperties.getConfiguredDataSource();
                        if (abort) {
                        	Log.debug("abort programeed. skipping worker alive update");
                        	continue;
                        }
                        if (bigsDataSource!=null && worker.currentEvaluation!=null) {                        	
                                synchronized(worker.currentEvaluation) {
                                	Evaluation storedEval = Evaluation.load(BIGS.globalProperties.getConfiguredDataSource(), currentEvaluation.getRowKey());
                                	if (!storedEval.getUuidStored().equals(Core.myUUID)) {
                                		Log.error("current evaluation has been updated by another worker. stopping this evaluation whenever possible. ");
                                	} else if (currentEvaluation!=null) {
                                		worker.currentEvaluation.markAlive(bigsDataSource);                           
                                	}
                                }
                                Log.debug("worker alive updated in evaluation "+worker.currentEvaluation.getRowKey());
                        }
                }               
        }
        
        public void stopRunning() {
                stop = true;
        }
	}	
	
}
