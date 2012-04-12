package bigs.modules.storage.hbase;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;

import bigs.api.core.BIGSParam;
import bigs.api.exceptions.BIGSException;
import bigs.api.storage.DataSource;
import bigs.api.storage.Table;
import bigs.core.BIGS;
import bigs.core.exceptions.BIGSTableExistsException;


public class HBaseDataSource implements DataSource {
	
	@BIGSParam
	public String zookeeperServerList = "localhost";
	
	Configuration hbaseConfig;
	HBaseAdmin hadmin;
	
	@Override
	public void initialize() {
		hbaseConfig = HBaseConfiguration.create();
		hbaseConfig.set("hbase.zookeeper.quorum", zookeeperServerList);
		hbaseConfig.set("hbase.client.keyvalue.maxsize","50000000");
		
		try {
			hadmin = new HBaseAdmin(hbaseConfig);
		} catch (MasterNotRunningException e) {
			throw new BIGSException ("master not running "+e.getMessage());
		} catch (ZooKeeperConnectionException e) {
			throw new BIGSException ("zookeeper connection exception "+e.getMessage());
		}
	}

	@Override
	public Boolean isAlive() {
		try {
			return hadmin.isMasterRunning();
		} catch (MasterNotRunningException e) {
			return false;
		} catch (ZooKeeperConnectionException e) {
			return false;
		}
	}
	
	@Override
	public Table getTable(String tableName) {
		return new HBaseTableWrapper(hbaseConfig, tableName);
	}
	
	@Override
	public Table createTable(String tableName, String[] columnFamilies) throws BIGSTableExistsException {
		HTableDescriptor hd = new HTableDescriptor(tableName);
		for (String family: columnFamilies) {
			HColumnDescriptor hc = new HColumnDescriptor(family);
			hd.addFamily(hc);
		}
		try {
			hadmin.createTable(hd);			
			return this.getTable(tableName);			
		} catch (IOException e) {
			if (e.getMessage().contains("TableExistsException")) {
				throw new BIGSTableExistsException("table "+tableName+" already exists.");
			} else {
				throw new BIGSException ("io exception creating table "+e.getMessage());
			}
		}
	}
	
	@Override
	public void dropTable(String tableName) {
		try {
			if (hadmin.isTableEnabled(tableName)) {
				hadmin.disableTable(tableName);
			}
			hadmin.deleteTable(tableName);
		} catch (IOException e) {
			throw new BIGSException("io exception deleting table "+e.getMessage());
		}
	}
	
	@Override
	public Boolean existsTable (String tableName) {
		try {
			HTableDescriptor[] hds = hadmin.listTables();
			for (HTableDescriptor hd: hds) {
				if (hd.getNameAsString().equals(tableName)) return true;
			}
			return false;
		} catch (IOException e) {
			throw new BIGSException("io exception checking table existance "+e.getMessage());
		}
	}
	
	@Override
	public long getTime() {
		
		// this is somewhat dirty, as it should not resort to stuff outside the API packages
		HBaseTableWrapper table = new HBaseTableWrapper(hbaseConfig, BIGS.tableName);
		
		HBasePutWrapper put = (HBasePutWrapper)table.createPutObject("counter");
		put.add("content", "dummy", "dummy".getBytes());
		table.put(put);
			

		HBaseGetWrapper get = (HBaseGetWrapper)table.createGetObject("counter");
		
		
		HBaseResultWrapper res = (HBaseResultWrapper)table.get(get);

		return (res.hbaseResult.raw()[0].getTimestamp());
	}

}
