package bigs.core.explorations;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.util.Bytes;

import bigs.api.core.Algorithm;
import bigs.api.core.BIGSParam;
import bigs.api.exceptions.BIGSException;
import bigs.api.storage.DataSource;
import bigs.api.storage.Get;
import bigs.api.storage.Put;
import bigs.api.storage.Result;
import bigs.api.storage.ResultScanner;
import bigs.api.storage.Scan;
import bigs.api.storage.Table;
import bigs.api.utils.TextUtils;
import bigs.core.BIGS;
import bigs.core.utils.Core;
import bigs.core.utils.Data;
import bigs.core.utils.Log;
import bigs.core.utils.Text;


public class Evaluation {
	
	public final static String tableName = "evals";
	public final static String[] columnFamilies = new String[]{"params", "metrics", "bigs" };	

	Long explorationNumber = 0L;
	Long repeatNumber      = 0L;
	Long configNumber      = 0L;
	Long stageNumber       = 0L;
	Long runNumber         = 0L;
	Long splitNumber       = 0L;
	
	Integer status         = STATUS_PENDING;
	
	Date lastUpdate  = new Date(Core.getTime());
	Long elapsedTime = 0L;
	
	String uuiStored = "";
	String hostnameStored = "";
	
	public final static Integer STATUS_PENDING    = 1;
	public final static Integer STATUS_INPROGRESS = 2;
	public final static Integer STATUS_DONE       = 3;
	public final static Integer STATUS_NONE       = 0;
	
	public final static String[] STATUS_STRINGS = new String[]{ "   NONE   ", 
																" PENDING  ", 
																"INPROGRESS", 
																"   DONE   " };
	Exploration parentExploration = null;
	
	Algorithm configuredAlgorithm = null;

	public Long getExplorationNumber() {
		return explorationNumber;
	}

	public void setExplorationNumber(Long explorationNumber) {
		this.explorationNumber = explorationNumber;
	}

	public Long getRepeatNumber() {
		return repeatNumber;
	}

	public void setRepeatNumber(Long repeatNumber) {
		this.repeatNumber = repeatNumber;
	}

	public Long getConfigNumber() {
		return configNumber;
	}

	public void setConfigNumber(Long configNumber) {
		this.configNumber = configNumber;
	}

	public Long getStageNumber() {
		return stageNumber;
	}

	public void setStageNumber(Long stageNumber) {
		this.stageNumber = stageNumber;
	}

	public Long getRunNumber() {
		return runNumber;
	}

	public void setRunNumber(Long runNumber) {
		this.runNumber = runNumber;
	}

	public Long getSplitNumber() {
		return splitNumber;
	}

	public void setSplitNumber(Long splitNumber) {
		this.splitNumber = splitNumber;
	}

	public Algorithm getConfiguredAlgorithm() {
		return configuredAlgorithm;
	}

	public void setConfiguredAlgorithm(Algorithm configuredAlgorithm) {
		this.configuredAlgorithm = configuredAlgorithm;
	}
	
	public Date getLastUpdate() {
		return lastUpdate;
	}
	
	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	
	public void setLastUpdateFromString(String lastUpdateString) {
		if (lastUpdateString==null || lastUpdateString.isEmpty()) {
			lastUpdate = null;
			return;
		}		try {
			lastUpdate = TextUtils.FULLDATE.parse(lastUpdateString);
		} catch (ParseException e) {
			throw new BIGSException("error parsing date "+lastUpdateString);
		}
	}
	
	public void setUuidStored(String uuidStored) {
		this.uuiStored = uuidStored;
	}
	
	/**
	 * this is the UUID of an evaluation retrieved from the DB that has not necessarily been created/updated
	 * by this process.
	 * @return
	 */
	public String getUuidStored() {
		return this.uuiStored;
	}
	
	public void setHostnameStored (String hostnameStored) {
		this.hostnameStored = hostnameStored;
	}

	/**
	 * this is the hostname of an evaluation retrieved from the DB that has not necessarily been created/updated
	 * by this process.
	 * @return
	 */
	public String getHostnameStored( ) {
		return this.hostnameStored;
	}
	public Long getElapsedTime() {
		return elapsedTime;
	}
	
	public void setElapsedTime(Long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}
	
	public void addToElapsedTime(Long time) {
		this.elapsedTime = this.elapsedTime + time;
	}
		
	
	public Integer getStatus() { 
		return status;
	}
	
	public void setStatus (Integer status) {
		this.status = status;
	}
	
	public Boolean isStatusPending() {
		return status == STATUS_PENDING;
	}
	
	public Boolean isStatusInProgress() {
		return status == STATUS_INPROGRESS;
	}
	
	public Boolean isStatusDone() {
		return status == STATUS_DONE;
	}
	
	public String getStatusAsString() {
		return STATUS_STRINGS[status];
	}
		
	public void setStatusFromString(String statusString) {
		this.status = STATUS_NONE;
		for (int i=0; i<STATUS_STRINGS.length; i++) {
			if (statusString.trim().equalsIgnoreCase(STATUS_STRINGS[i].trim())) {
				this.status = i;
			}
		}
	}
	
	static public String getStatusString(Integer status) {
		if (status > STATUS_STRINGS.length) {
			return STATUS_STRINGS[0];			
		}
		return STATUS_STRINGS[status];
	}
	
	public Exploration getParentExploration() {
		return this.parentExploration;
	}
	
	public void setParentExploration(Exploration parentExploration ) {
		this.parentExploration = parentExploration;
	}
	
	/**
	 * returns true if all numbers (repeat, config, etc.) are zero except the exploration Number
	 * @return
	 */
	public Boolean isExploration() {
		return (this.explorationNumber!=0 && this.repeatNumber==0 && 
				this.configNumber==0     && this.stageNumber==0 && 
				this.runNumber==0        && this.splitNumber==0);
	}
	
	/**
	 * returns true if all numbers are zero except the exploration and repeat number
	 * @return
	 */
	public Boolean isRepeat() {
		return (this.explorationNumber!=0 && this.repeatNumber!=0 && 
				this.configNumber==0     && this.stageNumber==0 && 
				this.runNumber==0        && this.splitNumber==0);
	}
	
	/**
	 * returns true if all numbers are zero except the exploration, repeat and config number
	 * @return
	 */
	public Boolean isConfig() {
		return (this.explorationNumber!=0 && this.repeatNumber!=0 && 
				this.configNumber!=0     && this.stageNumber==0 && 
				this.runNumber==0        && this.splitNumber==0);
	}

	/**
	 * returns true if all numbers are zero except the exploration, repeat, config and stage number
	 * @return
	 */
	public Boolean isStage() {
		return (this.explorationNumber!=0 && this.repeatNumber!=0 && 
				this.configNumber!=0     && this.stageNumber!=0 && 
				this.runNumber==0        && this.splitNumber==0);
	}

	/**
	 * returns true if all numbers are zero except the exploration, repeat, config, stage and run number
	 * @return
	 */
	public Boolean isRun() {
		return (this.explorationNumber!=0 && this.repeatNumber!=0 && 
				this.configNumber!=0     && this.stageNumber!=0 && 
				this.runNumber!=0        && this.splitNumber==0);
	}

	/**
	 * returns true if no numbers are zero
	 * @return
	 */
	public Boolean isSplit() {
		return (this.explorationNumber!=0 && this.repeatNumber!=0 && 
				this.configNumber!=0     && this.stageNumber!=0 && 
				this.runNumber!=0        && this.splitNumber!=0);
	}
	
	/**
	 * returns a string with this eval's type (config, repeat, etc.)
	 * @return
	 */
	public String getTypeString() {
		if (this.isExploration()) return "EXPL";
		if (this.isRepeat())      return "RPT ";
		if (this.isConfig())      return "CFG ";
		if (this.isStage())       return "STGE";
		if (this.isRun())         return "RUN ";
		if (this.isSplit())       return "SPLT";
		else                      return "    ";
	}

	/**
	 * returns as string representation of the rowkey corresponding to this evaluation
	 * @return
	 */
	public String getRowKey() {
		StringBuffer sb = new StringBuffer();
		sb.append(Text.zeroPad(this.explorationNumber)).append(".");
		sb.append(Text.zeroPad(this.repeatNumber)).append(".");
		sb.append(Text.zeroPad(this.configNumber)).append(".");
		sb.append(Text.zeroPad(this.stageNumber)).append(".");
		sb.append(Text.zeroPad(this.runNumber)).append(".");
		sb.append(Text.zeroPad(this.splitNumber));
		return sb.toString();
	}
	
	/**
	 * builds an empty Evaluation object from the string representation of its rowkey.
	 * Leaves the configured algorithm empty (null)
	 * @param k
	 * @return
	 */
	static public Evaluation fromRowKey(String k) {
		
		Evaluation r = new Evaluation();
		
		String[] s = k.split("\\.");
		if (s.length!=6) {
			throw new BIGSException("incorrect evaluation rowkey specification");
		}

		r.explorationNumber = new Long(s[0]);
		r.repeatNumber      = new Long(s[1]);
		r.configNumber      = new Long(s[2]);
		r.stageNumber       = new Long(s[3]);
		r.runNumber         = new Long(s[4]);
		r.splitNumber       = new Long(s[5]);
		
		return r;
	}
	
	/**
	 * builds an Evaluation object from a Result object obtained from a Scan or
	 * Get in the underlying storage
	 * @param result
	 * @return
	 */
	static public Evaluation fromResultObject(Result result) {
		Evaluation eval = Evaluation.fromRowKey(result.getRowKey());
		
		
		byte[] stat = result.getValue("bigs", "status");
		if (stat!=null) {
			eval.setStatusFromString(new String(stat));
		}
		
		byte[] etime = result.getValue("bigs", "elapsedtime");
		if (etime!=null) {
			eval.setElapsedTime(new Long(new String(etime)));
		}

		byte[] lastupdate = result.getValue("bigs", "lastupdate");
		if (lastupdate!=null) {
			eval.setLastUpdateFromString(new String(lastupdate));
		}
		
		byte[] suuid = result.getValue("bigs", "uuid");
		if (suuid!=null) {
			eval.setUuidStored(new String(suuid));
		}

		byte[] shostname = result.getValue("bigs", "hostname");		
		if (shostname!=null) {
			eval.setHostnameStored(new String(shostname));
		}
				
		byte[] classNameBytes = result.getValue("params", "class");
		if (classNameBytes!=null) {
			Map<String, String> fieldValues = result.getFamilyMap("params");		
			Algorithm configuredAlgorithm = Core.getConfiguredObject(new String(classNameBytes), Algorithm.class, fieldValues);
			eval.setConfiguredAlgorithm(configuredAlgorithm);
		}		
		return eval;
	}
	
	
	/**
	 * Loads an evaluation from a datasource
	 * 
	 * @param dataSource the datasource object where to look up the evaluation
	 * @param rowKey the rowkey of the evaluation to retrieve
	 * @return
	 */
	static public Evaluation load (DataSource dataSource, String rowKey) {
		Table table = dataSource.getTable(Evaluation.tableName);
		Get get = Evaluation.fillGetObject(table.createGetObject(rowKey));
		Result result = table.get(get);
		Evaluation r = Evaluation.fromResultObject(result);
		return r;
	}
	
	/**
	 * saves this evaluation in the underlying datasource
	 * @param dataSource the datasource
	 */
	public void save() {
    	DataSource dataSource = BIGS.globalProperties.getConfiguredDataSource();
    	Table table = dataSource.getTable(Evaluation.tableName);
    	table.put(Data.fillInHostMetadata(this.fillPutObject(table.createPutObject(this.getRowKey()))));				
	}
	
	/**
	 * time stamps this evaluation in the underlying storage by udpating the content
	 * of the column 'bigs:alive' with the current date only if the existing uuid is the
	 * same of this process.
	 * 
	 * @param dataSource the datasource object where this evaluation is stored
	 * @return true if the update was successful, false otherwise (if the existing uuid
	 *         is different, meaning that somebody else is working on this evaluation)
	 */
	public Boolean markAlive(DataSource dataSource) {
		Table table = dataSource.getTable(Evaluation.tableName);
		Put put = table.createPutObject(this.getRowKey());
		this.lastUpdate = new Date(Core.getTime());
		put = this.fillPutObject(put);
		put = Data.fillInHostMetadata(put);		
		Boolean r = table.checkAndPut(this.getRowKey(), "bigs", "uuid", Core.myUUID.getBytes(), put);
		return r;
	}
	
	/**
	 * Fills a Put object leaving it ready to persist this evaluation into the underlying storage
	 * @param put the Put object to fill in
	 * @return the same object filled in
	 */
	public Put fillPutObject (Put put) {		

		if (configuredAlgorithm!=null) {
			Map<String, String> params = Core.getObjectAnnotatedFieldsAsString(configuredAlgorithm, BIGSParam.class);
			put.add("params", "class", Bytes.toBytes(configuredAlgorithm.getClass().getName()));
			for (String key: params.keySet()) {
				put.add("params", key, Bytes.toBytes(params.get(key)));
			}		
		}
		
		put.add("bigs","status", Bytes.toBytes(this.getStatusAsString()));
		
		lastUpdate = new Date(Core.getTime());
    	
		put.add("bigs", "lastupdate", Bytes.toBytes(TextUtils.FULLDATE.format(this.lastUpdate)));
    		
    	if (this.elapsedTime!=null) {
    		put.add("bigs", "elapsedtime", Bytes.toBytes(this.elapsedTime.toString()));
    	}
		return put;
	}
	
	/**
	 * Fills a Get object leaving it ready to completely retrieve this Evaluation object from the underlying storage
	 * @param get the Get object to fill in
	 * @return the same Get object filled in
	 */
	public static Get fillGetObject (Get get) {
		
		for (String family: Evaluation.columnFamilies) {
			get.addFamily(family);
		}
		get.addColumn("bigs", "timestart");
		get.addColumn("bigs", "timedone");
		get.addColumn("bigs", "lastupdate");
		get.addColumn("bigs", "hostname");
		get.addColumn("bigs", "uuid");
		return get;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getRowKey()).append(" ");
		sb.append(this.getTypeString()).append(" ");
		sb.append("[").append(this.getStatusAsString()).append("] ");
		if (configuredAlgorithm!=null) {
			sb.append(this.configuredAlgorithm.toString());
		}
		return sb.toString();
	}

	
	public Evaluation clone() {
		Evaluation r = new Evaluation();
		r.configNumber = this.configNumber;
		r.configuredAlgorithm = this.configuredAlgorithm;
		r.explorationNumber = this.explorationNumber;
		r.lastUpdate = this.lastUpdate;
		r.repeatNumber = this.repeatNumber;
		r.runNumber = this.runNumber;
		r.splitNumber = this.splitNumber;
		r.stageNumber = this.stageNumber;
		r.status = this.status;
		r.elapsedTime = this.elapsedTime;
		return r;
	}
	
	Boolean bothNullOrEqual(Object o1, Object o2) {
		if (o1==null && o2==null) return true;
		if (o1==null || o2==null) return false;
		if (!o1.equals(o2)) return false;
		return true;
	}
	
	public Boolean equals(Evaluation eval) {
		if (!bothNullOrEqual(this.configNumber, eval.configNumber)) return false;
		if (!bothNullOrEqual(this.configuredAlgorithm, eval.configuredAlgorithm)) return false;
		if (!bothNullOrEqual(this.explorationNumber, eval.explorationNumber)) return false;
		if (!bothNullOrEqual(this.repeatNumber, eval.repeatNumber)) return false;
		if (!bothNullOrEqual(this.runNumber, eval.runNumber)) return false;
		if (!bothNullOrEqual(this.splitNumber, eval.splitNumber)) return false;
		if (!bothNullOrEqual(this.stageNumber, eval.stageNumber)) return false;
		if (!bothNullOrEqual(this.status, eval.status)) return false;
		if (!bothNullOrEqual(this.elapsedTime, eval.elapsedTime)) return false;
		
		return true;
	}
}
