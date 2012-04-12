package bigs.modules.storage.hbase;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.util.Bytes;

import bigs.api.exceptions.BIGSException;
import bigs.api.storage.Get;
import bigs.api.storage.Put;
import bigs.api.storage.Result;
import bigs.api.storage.ResultScanner;
import bigs.api.storage.Scan;
import bigs.api.storage.Table;
import bigs.api.storage.Update;


public class HBaseTableWrapper implements Table {

	org.apache.hadoop.hbase.client.HTable hbaseTable;
	
	public HBaseTableWrapper (Configuration conf, String tableName) {
		try {
			hbaseTable = new org.apache.hadoop.hbase.client.HTable(conf, Bytes.toBytes(tableName));
		} catch (IOException e) {		
			throw new BIGSException("IO Exception initializing HBase table "+tableName+". "+e.getMessage());
		}
	}
	
	@Override
	public Update createUpdateObject(String key) {
		return new HBasePutWrapper(key);
	}

	@Override
	public Put createPutObject(String key) {
		return new HBasePutWrapper(key);
	}

	@Override
	public void put(Put put) {
		if (! (put instanceof HBasePutWrapper)) {
			throw new BIGSException ("framework is calling HBase datasource implementation with non HBase generated objects");
		}
		org.apache.hadoop.hbase.client.Put hbasePut = ((HBasePutWrapper)put).hbasePut;
		try {
			hbaseTable.put (hbasePut);
		} catch (IOException e) {
			throw new BIGSException("IO Exception putting into hbase. "+e.getMessage());
		}

	}
	
	@Override
	public void update(Update update) {
		if (! (update instanceof HBasePutWrapper)) {
			throw new BIGSException ("framework is calling HBase datasource implementation with non HBase generated objects");
		}
		
		HBasePutWrapper hp = (HBasePutWrapper)update;
		this.put(hp);
	}
	
	@Override
	public boolean checkAndPut(String row, String columnFamily, String columnName, byte[] value, Put put) {
		if (! (put instanceof HBasePutWrapper)) {
			throw new BIGSException ("framework is calling HBase datasource implementation with non HBase generated objects");
		}
		org.apache.hadoop.hbase.client.Put hbasePut = ((HBasePutWrapper)put).hbasePut;
		try {
			return hbaseTable.checkAndPut(row.getBytes(), columnFamily.getBytes(), columnName.getBytes(), value, hbasePut);
		} catch (IOException e) {
			throw new BIGSException("IO Exception putting into hbase. "+e.getMessage());
		}
	}


	@Override
	public Get createGetObject(String key) {
		return new HBaseGetWrapper(key);
	}

	@Override
	public Result get(Get get) {
		if (! (get instanceof HBaseGetWrapper)) {
			throw new BIGSException ("framework is calling HBase datasource implementation with non HBase generated objects");
		}
		org.apache.hadoop.hbase.client.Get hbaseget = ((HBaseGetWrapper)get).hbaseGet;
		
		try {
			Result r = new HBaseResultWrapper(hbaseTable.get(hbaseget));
			return r;
		} catch (IOException e) {
			throw new BIGSException("IO Exception getting into hbase. "+e.getMessage());
		}
		
	}
	
	@Override
	public Scan createScanObject() {
		return new HBaseScanWrapper();
	}
	
	@Override 
	public ResultScanner getScan(Scan scan) {
		if (! (scan instanceof HBaseScanWrapper)) {
			throw new BIGSException ("framework is calling HBase datasource implementation with non HBase generated objects");
		}
		org.apache.hadoop.hbase.client.Scan hbasescan = ((HBaseScanWrapper)scan).hbaseScan;
		try {
			return new HBaseResultScannerWrapper(hbaseTable.getScanner(hbasescan));
		} catch (IOException e) {
			throw new BIGSException("io exception scanning HBase table. "+e.getMessage());
		}
		
	}
	
	@Override
	public String[] getColumnFamilies() {
		try {
			HColumnDescriptor[] columnFamilies = hbaseTable.getTableDescriptor().getColumnFamilies();
			String[] r = new String[columnFamilies.length];
			for (int i=0; i<columnFamilies.length; i++) {
				r[i] = columnFamilies[i].getNameAsString();
			}
			return r;
		} catch (IOException e) {
			throw new BIGSException("io exception retrieving HTable information "+e.getMessage());
		}
	}
	
	@Override
	public long incrementColumnValue(String row, String columnFamily, String columnName, long amount) {
		try {
			return hbaseTable.incrementColumnValue(Bytes.toBytes(row),Bytes.toBytes(columnFamily), Bytes.toBytes(columnName), amount);
		} catch (IOException e) {
			throw new BIGSException("io exception when incrementing value "+e.getMessage());
		}
	}
	

}
