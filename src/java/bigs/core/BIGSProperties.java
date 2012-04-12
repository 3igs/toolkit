package bigs.core;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import bigs.api.exceptions.BIGSException;
import bigs.api.storage.DataSource;
import bigs.api.utils.TextUtils;
import bigs.core.exceptions.BIGSPropertyNotFoundException;
import bigs.core.utils.Core;
import bigs.core.utils.Data;
import bigs.core.utils.Log;


public class BIGSProperties extends Properties {

	public static final Boolean DONOT_CREATE_TABLES  = false;
	
	private static final long serialVersionUID = 1L;
	static String ldatasource = "datasource";
	static String lprefix     = "bigs";
	static String lalive      = "worker.alive.interval";
	static String lcleanup    = "worker.clean.interval";
	static String lsleep      = "worker.sleep.interval";
	
	public static Long WORKER_ALIVE_INTERVAL = 60000L;
	public static Long WORKER_CLEAN_INTERVAL = 120000L;
	public static Long WORKER_SLEEP_INTERVAL = 20000L;

	static DataSource pooledDataSource=null;
	
	/**
	 * wrapper overloaded method with default behavior to create non existing tables
	 * @return
	 */
	public DataSource getConfiguredDataSource() {
		return this.getConfiguredDataSource(true);
	}
		
	/**
	 * returns the BIGS datasource object declared in this exploration configured with 
	 * the exploration parameters. Keeps a single element pool to avoid creating
	 * many connections
	 * @param initTables if true non existing system tables will be created
	 * @return
	 */
	public DataSource getConfiguredDataSource(Boolean initTables) {
		// returned pooled connection if it is working
		if (pooledDataSource!=null && pooledDataSource.isAlive()) {
			return pooledDataSource;
		}
		
		// else retrieve its definition from properties file and create it
		try {
			pooledDataSource =
					Core.getConfiguredObject(
							ldatasource, DataSource.class,this, lprefix);
		} catch (BIGSPropertyNotFoundException e) {
			throw new BIGSException("error in properties: "+e.getMessage());
		}
		pooledDataSource.initialize();
		if (initTables) {
			Data.initBIGSTables(pooledDataSource);
		}

		// calibrate timeoffset
		Log.debug("calibrating time offset");
		Date date = new Date(Core.calibrateTime(pooledDataSource));
		Log.info("reference time is "+TextUtils.FULLDATE.format(date));
		return pooledDataSource;		
	}
	
	/**
	 * returns the properties list as found in the original source
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		@SuppressWarnings("unchecked")
		Enumeration<String> e = (Enumeration<String>)this.propertyNames();
    	for (; e.hasMoreElements(); ) {
    		String key = e.nextElement();
    		sb.append(key).append(": ").append(this.getProperty(key)).append("\n");
    	}
    	return sb.toString();
	}
	
	public void load(Reader reader) throws IOException {
		super.load(reader);
				
		String p;
		p = this.getProperty(lprefix+"."+lalive);
		if (p!=null) WORKER_ALIVE_INTERVAL = new Long(p)*1000;

		p = this.getProperty(lprefix+"."+lsleep);
		if (p!=null) WORKER_SLEEP_INTERVAL = new Long(p)*1000;
		
		p = this.getProperty(lprefix+"."+lcleanup);
		if (p!=null) WORKER_CLEAN_INTERVAL = new Long(p)*1000;
	}
	
	
	public static BIGSProperties fromString(String s) {
        BIGSProperties props = new BIGSProperties();
        try {
            props.load(new StringReader(s));
        } catch (IOException ex) {
            throw new BIGSException ("cannot load BIGS properties from string. "+ex.getMessage());
        }
        return props;
    }
}
