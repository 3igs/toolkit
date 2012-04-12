package bigs.core.explorations;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.hadoop.hbase.util.Bytes;

import bigs.api.core.Configurable;
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
import bigs.core.utils.Text;


public class Exploration extends Properties {
	
	private static final long serialVersionUID = 1L;
	
	public final static String tableName = "explorations";
	public final static String[] columnFamilies = new String[]{"spec", "bigs"};

	static String[] allowedStages = new String[]{ "01", "02", "03", "04" };
	static String lstage      = "stage";
	
	public final static Integer STATUS_NONE   = 0;
	public final static Integer STATUS_NEW    = 1;
	public final static Integer STATUS_READY  = 2;
	public final static Integer STATUS_ACTIVE = 3;
	public final static Integer STATUS_DONE   = 4;
	
	public final static String[] STATUS_STRINGS = new String[]{ "  NONE   ",
																"   NEW   ", 
																"  READY  ", 
																"  ACTIVE ", 
																"   DONE  "};

	Long explorationNumber = 0L;
	Integer status = STATUS_NEW;
	
	Date timeStart = null;
	Date timeDone  = null;
	
	
	List<ExplorationStage> declaredStages = new ArrayList<ExplorationStage>();

	public Long getExplorationNumber() {
		return explorationNumber;
	}
	
	public Date getTimeStart() {
		return timeStart;
	}

	public void setTimeStart(Date timeStart) {
		this.timeStart = timeStart;
	}
	
	public void setTimeStartFromTimeReference() {
		this.timeStart = new Date(Core.getTime());
	}

	public void setTimeStartFromString(String timeStartString) {
		if (timeStartString==null || timeStartString.isEmpty()) {
			timeStart = null;
			return;
		}
		try {
			timeStart = TextUtils.FULLDATE.parse(timeStartString);
		} catch (ParseException e) {
			throw new BIGSException("error parsing date "+timeStartString);
		}
	}

	public Date getTimeDone() {
		return timeDone;
	}

	public void setTimeDone(Date timeDone) {
		this.timeDone = timeDone;
	}
	
	public void setTimeDoneFromTimeReference() {
		this.timeDone = new Date(Core.getTime());
	}

	public void setTimeDoneFromString(String timeDoneString) {
		if (timeDoneString==null || timeDoneString.isEmpty()) {
			timeDone = null;
			return;
		}
		try {
			timeDone = TextUtils.FULLDATE.parse(timeDoneString);
		} catch (ParseException e) {
			throw new BIGSException("error parsing date "+timeDoneString);
		}
	}

	public Integer getStatus() {
		return status;
	}
	
	public void setStatus(Integer status) {
		this.status = status;
	}
	
	public Boolean isStatusNew() {
		return status == STATUS_NEW;
	}
	
	public Boolean isStatusReady() {
		return status == STATUS_READY;
	}
	
	public Boolean isStatusActive() {
		return status == STATUS_ACTIVE;
	}
	
	public Boolean isStatusDone() {
		return status == STATUS_DONE;
	}
	
	public Boolean isStatusNone() {
		return status == STATUS_NONE;
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
	/**
	 * returns the stages declared in this exploration. 
	 * Stages are parsed and created in the load method.
	 * @return
	 */
	public List<ExplorationStage> getStages() {
		return declaredStages;
	}
	
	public void cleanExplorationNumber() {
		this.explorationNumber = 0L;
	}
	
	/**
	 * loads properties from an arbitrary source and creates stages objects
	 * @param f a Reader containing the properties source
	 */
	@Override
    public void load(Reader f) {
		try {
			super.load(f);    	    	    	
		} catch (IOException e) {
			throw new BIGSException("error reading properties from "+f.toString()+", "+e.getMessage());
		}
    	
    	List<String> stages = new ArrayList<String>();
    	for (String stage: allowedStages ) {
    		if (this.hasAnyPropertyWithPrefix(lstage+"."+stage)) {
    			stages.add(stage);
    		}
    		else break;
    	}
    	
    	for (String stage: stages) {
    		ExplorationStage eStage = new ExplorationStage(this, stage);
    		declaredStages.add(eStage);
    	}
    }	
	

	
	/**
	 * creates a new properties object from a Result object previously
	 * retrieved from the underlying storage
	 * 
	 * @param source the Result object containing the exploration information
	 * @return a new Exploration properties object
	 */
	public static Exploration fromResultObject (Result source) {
		Exploration r = new Exploration();
		String stringProperties = new String(source.getValue("spec", "properties"));
		r.setStatusFromString(new String(source.getValue("bigs", "status")));
		byte[] tstart = source.getValue("bigs", "timestart");
		byte[] tdone  = source.getValue("bigs", "timedone");
		if (tstart!=null) {
			r.setTimeStartFromString(new String(tstart));
		}
		if (tdone!=null) {
			r.setTimeDoneFromString(new String(tdone));
		}
		r.load(new StringReader(stringProperties));
		r.explorationNumber = new Long(source.getRowKey());
		return r;
	}
	
	/**
	 * creates a new properties object by retrieving it as stored in the underlying db
	 * @param explorationNumber the exploration number to retrieve
	 * @return the retrieved exploration object
	 */
	public static Exploration fromExplorationNumber (Long explorationNumber) {
    	DataSource d = BIGS.globalProperties.getConfiguredDataSource();
		Table table = d.getTable(Exploration.tableName);

		Get   get   = table.createGetObject(Text.zeroPad(explorationNumber));
		get.addColumn("spec", "properties");
		get.addColumn("bigs", "status");
		get.addColumn("bigs", "timestart");
		get.addColumn("bigs", "timedone");
		Exploration r = Exploration.fromResultObject(table.get(get));
		return r;
	}
	
	/**
	 * stores a configuration object in the DB
	 * @param confNumber
	 * @param obj
	 */
	public void storeConfiguration (Integer confNumber, Configurable obj) {
		DataSource dataSource = BIGS.globalProperties.getConfiguredDataSource();
		
		Map<String, String> params = Core.getObjectAnnotatedFieldsAsString(obj, BIGSParam.class);
		
		Table table = dataSource.getTable(Evaluation.tableName);
		Put put = table.createPutObject(confNumber.toString());
		put.add("params", "class", Bytes.toBytes(obj.getClass().getName()));
		for (String key: params.keySet()) {
			put.add("params", key, Bytes.toBytes(params.get(key)));
		}
		table.put(put);
		
	}
	
	/**
	 * returns the properties list as found in the original source
	 */
	public String getPropertiesAsString() {
		StringBuffer sb = new StringBuffer();
		@SuppressWarnings("unchecked")
		Enumeration<String> e = (Enumeration<String>)this.propertyNames();
    	for (; e.hasMoreElements(); ) {
    		String key = e.nextElement();
    		sb.append(key).append(": ").append(this.getProperty(key)).append("\n");
    	}
    	return sb.toString();
	}
	
    /**
     * returns true is there exists any property with the given prefix
     * @param prefix
     * @return
     */
    Boolean hasAnyPropertyWithPrefix(String prefix) {
    	@SuppressWarnings("unchecked")
		Enumeration<String> e = (Enumeration<String>)this.propertyNames();
    	for (; e.hasMoreElements(); ) {
    		String propName = e.nextElement();
    	    if (propName.startsWith(prefix)) return true;
    	}    	
    	return false;
    }
    
    /**
     * saves an exploration in the data source configured in the global properties
     */
    public void save() {
    	DataSource d = BIGS.globalProperties.getConfiguredDataSource();
		Table table = d.getTable(Exploration.tableName);

		if (explorationNumber==null || explorationNumber==0) {
    		explorationNumber = this.getNextFreeExporationNumber();
    	}
		Put put = table.createPutObject(Text.zeroPad(explorationNumber));
    	put.add("bigs", "uuid", Bytes.toBytes(Core.myUUID));
    	put.add("spec", "properties", Bytes.toBytes(this.getPropertiesAsString()));
    	put.add("bigs", "status", Bytes.toBytes(this.getStatusAsString().trim()));
    	if (this.timeStart!=null) {
    		put.add("bigs", "timestart", Bytes.toBytes(TextUtils.FULLDATE.format(this.timeStart)));
    	} else {
    		put.add("bigs", "timestart", Bytes.toBytes(""));
    	}
    	if (this.timeDone!=null) {
    		put.add("bigs", "timedone", Bytes.toBytes(TextUtils.FULLDATE.format(this.timeDone)));
    	} else {
    		put.add("bigs", "timedone", Bytes.toBytes(""));    		
    	}
    	table.put(put);
    }
    
    public String toString() {
    	StringBuffer sb = new StringBuffer();
    	sb.append("[").append(Text.zeroPad(this.explorationNumber)).append("] [").append(this.getStatusAsString()).append("]");
    	return sb.toString();
    }
    
    /**
     * returns the greatest + 1 value for a rowkey in the explorations table
     * @return
     */
    Long getNextFreeExporationNumber() {
    	
    	DataSource d = BIGS.globalProperties.getConfiguredDataSource();
		Table table = d.getTable(BIGS.tableName);
		return table.incrementColumnValue("counters", "content", "exploration", 1);
    }    
    
    public static List<Exploration> getExplorationsForStatus (Integer status ) {
		DataSource dataSource = BIGS.globalProperties.getConfiguredDataSource();
		List<Exploration> r = new ArrayList<Exploration> ();
		Table table = dataSource.getTable(Exploration.tableName);
		Scan scan = table.createScanObject();
		for (String family: Exploration.columnFamilies) {
			scan.addFamily(family);
		}
		scan.setFilterByColumnValue("bigs", "status", Bytes.toBytes(STATUS_STRINGS[status].trim()));
		ResultScanner rs = table.getScan(scan);
		try {
			for (Result rr = rs.next(); rr!=null; rr = rs.next()) {					
				Exploration ev = Exploration.fromResultObject(rr);
				r.add(ev);
			}
		} finally {
			rs.close();
		}		
		return r;
    	
    }
    
	
    List<Evaluation> cachedEvaluations = null;
	/**
	 * Retrieves from the underlying storage all evaluations corresponding to this exploration.
	 * Caches the result unless specified in the arguments
	 * @param useCache if false will always get the evals from the DB
	 * @return the list of evaluations of this exploration
	 */
	public List<Evaluation> getAllEvaluations (Boolean useCache) {
		if (cachedEvaluations!=null && useCache) return cachedEvaluations;
		DataSource dataSource = BIGS.globalProperties.getConfiguredDataSource();
		List<Evaluation> r = new ArrayList<Evaluation> ();
		Table table = dataSource.getTable(Evaluation.tableName);
		Scan scan = table.createScanObject();
		scan.setStartRow(Text.zeroPad(this.explorationNumber));
		scan.setStopRow(Text.zeroPad(this.explorationNumber+1));
		for (String family: Evaluation.columnFamilies) {
			scan.addFamily(family);
		}
		ResultScanner rs = table.getScan(scan);
		try {
			for (Result rr = rs.next(); rr!=null; rr = rs.next()) {					
				Evaluation ev = Evaluation.fromResultObject(rr);
				r.add(ev);
			}
		} finally {
			rs.close();
		}	
		cachedEvaluations = r;
		return cachedEvaluations;
	}    

	public String getInfo() {
		List<Evaluation> evals = this.getAllEvaluations(Core.NOCACHE);

		Integer totalRepeats = 0; Integer repeatsDone = 0; Integer repeatsInProgress = 0;
		Integer totalConfigs = 0; Integer configsDone = 0; Integer configsInProgress = 0;
		Integer totalStages  = 0; Integer stagesDone  = 0; Integer stagesInProgress  = 0;
		Integer totalRuns    = 0; Integer runsDone    = 0; Integer runsInProgress    = 0;
		Integer totalSplits  = 0; Integer splitsDone  = 0; Integer splitsInProgress  = 0;
		Integer totalEvals   = 0; Integer evalsDone   = 0; Integer evalsInProgress   = 0;
		
		Long totalComputeTime = 0L;
		
		Map<String, Long> computePerHost = new HashMap<String ,Long>();
		Map<String, Long> evalsPerHost   = new HashMap<String, Long>();
		
		for (Evaluation eval: evals) {
			
			Long computeTime = eval.getElapsedTime();
			if (computeTime!=null) {
				totalComputeTime = totalComputeTime +computeTime;
			}
			
			String hostnameStored = eval.getHostnameStored();
			if (hostnameStored!=null && !hostnameStored.isEmpty()) {
				Long t = computePerHost.get(hostnameStored);
				if (t==null) t=0L;
				t = t + eval.getElapsedTime();
				computePerHost.put(hostnameStored, t);
				
				Long n = evalsPerHost.get(hostnameStored);
				if (n==null) n=0L;
				n++;
				evalsPerHost.put(hostnameStored, n);
			}
			
			totalEvals ++ ;
			if (eval.isStatusDone()) evalsDone++;
			if (eval.isStatusInProgress()) evalsInProgress++;
			
			if (eval.isRepeat()) {
				totalRepeats++;
				if (eval.isStatusDone()) repeatsDone++;
				if (eval.isStatusInProgress()) repeatsInProgress++;
			}
			if (eval.isConfig()) {
				totalConfigs++;
				if (eval.isStatusDone()) configsDone++;
				if (eval.isStatusInProgress()) configsInProgress++;
			}
			if (eval.isStage()) {
				totalStages++;
				if (eval.isStatusDone()) stagesDone++;
				if (eval.isStatusInProgress()) stagesInProgress++;
			}
			if (eval.isRun()) {
				totalRuns++;
				if (eval.isStatusDone()) runsDone++;
				if (eval.isStatusInProgress()) runsInProgress++;
			}
			if (eval.isSplit()) {
				totalSplits++;
				if (eval.isStatusDone()) splitsDone++;
				if (eval.isStatusInProgress()) splitsInProgress++;
			}		
						
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append("Repeats:     "+Text.rightJustify(totalRepeats.toString(), 5)+" / "+Text.rightJustify(repeatsDone.toString(), 5)+" done / "+Text.rightJustify(repeatsInProgress.toString(), 5)+" in progress \n");
		sb.append("Configs:     "+Text.rightJustify(totalConfigs.toString(), 5)+" / "+Text.rightJustify(configsDone.toString(), 5)+" done / "+Text.rightJustify(configsInProgress.toString(), 5)+" in progress \n");
		sb.append("Stages:      "+Text.rightJustify(totalStages.toString(), 5)+" / "+Text.rightJustify(stagesDone.toString(), 5)+" done / "+Text.rightJustify(stagesInProgress.toString(), 5)+" in progress \n");
		sb.append("Runs:        "+Text.rightJustify(totalRuns.toString(), 5)+" / "+Text.rightJustify(runsDone.toString(), 5)+" done / "+Text.rightJustify(runsInProgress.toString(), 5)+" in progress \n");
		sb.append("Splits:      "+Text.rightJustify(totalSplits.toString(), 5)+" / "+Text.rightJustify(splitsDone.toString(), 5)+" done / "+Text.rightJustify(splitsInProgress.toString(), 5)+" in progress \n");
		sb.append("--------\n");
		Double pctDone = 100 * new Double(evalsDone) / new Double(totalEvals);
		Double pctInProgress = 100 * new Double(evalsInProgress) / new Double(totalEvals);
		sb.append("Exploration: "+Text.rightJustify(totalEvals.toString(), 5)+" / "+Text.rightJustify(evalsDone.toString(), 5)+" done ("+TextUtils.F2.format(pctDone)+"%) / "+Text.rightJustify(evalsInProgress.toString(), 5)+" in progress ("+TextUtils.F2.format(pctInProgress)+"%) \n");
		sb.append("\n");
		if (this.timeStart!=null) sb.append("start time   ").append(TextUtils.FULLDATE.format(this.timeStart)).append("\n");
		if (this.timeDone!=null)  sb.append("time done    ").append(TextUtils.FULLDATE.format(this.timeDone)).append("\n");
		if (this.timeDone!=null && this.timeStart!=null) {
			Long elapsedTime = this.timeDone.getTime() - this.timeStart.getTime();
			sb.append("elapsed time ").append(Text.timeToString(elapsedTime)).append(" (").append(elapsedTime).append(" milisecs)\n"); 
		}
		
		if (totalComputeTime!=0) {
			sb.append("compute time ").append(Text.timeToString(totalComputeTime)).append(" (").append(totalComputeTime).append(" milisecs)\n"); 
			sb.append("\n");
			sb.append("compute time per participating host:\n");
			int hosts = 0;
			for (String host: computePerHost.keySet()) {
				Long t = computePerHost.get(host);
				Long n = evalsPerHost.get(host);
				if (t!=0L) {
					Double pct = 100 * new Double(t)/new Double(totalComputeTime);
					sb.append(Text.leftJustify(host, 30)).append(Text.leftJustify(Text.timeToString(computePerHost.get(host)),13));
					sb.append(" (").append(Text.leftJustify(TextUtils.F2.format(pct)+" %)",10));
					hosts ++;
					if (n!=0L) {
						sb.append(" evals: ").append(n);
					}
					sb.append("\n");
				}
			}
			sb.append("\nparticipating hosts "+hosts);
		}

		
		return sb.toString();
	}
	
}
