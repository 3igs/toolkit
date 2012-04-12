package bigs.modules.storage.hbase;

import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.util.Bytes;

import bigs.api.storage.Scan;
import bigs.core.utils.Log;


public class HBaseScanWrapper implements Scan {

	org.apache.hadoop.hbase.client.Scan hbaseScan;

	public HBaseScanWrapper() {
		hbaseScan = new org.apache.hadoop.hbase.client.Scan();
	}
	
	@Override
	public void setStartRow(String startRow) {
		hbaseScan.setStartRow(Bytes.toBytes(startRow));
	}

	@Override
	public void setStopRow(String stopRow) {
		hbaseScan.setStopRow(Bytes.toBytes(stopRow));
	}

	@Override
	public void addColumn(String columnFamily, String columnName) {
		hbaseScan.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(columnName));
	}

	@Override
	public void addFamily(String columnFamily) {
		hbaseScan.addFamily(Bytes.toBytes(columnFamily));
	}

	@Override
	public void setFilterByColumnValue(String columnFamily, String columnName, byte[] value) {
		SingleColumnValueFilter filter = new SingleColumnValueFilter(
				Bytes.toBytes(columnFamily), Bytes.toBytes(columnName), 
				CompareFilter.CompareOp.EQUAL, value);	
		
		hbaseScan.setFilter(filter);		
	}

}
