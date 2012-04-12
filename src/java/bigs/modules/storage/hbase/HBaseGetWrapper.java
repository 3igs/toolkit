package bigs.modules.storage.hbase;

import org.apache.hadoop.hbase.util.Bytes;

import bigs.api.storage.Get;


public class HBaseGetWrapper implements Get {

	org.apache.hadoop.hbase.client.Get hbaseGet;
	
	public HBaseGetWrapper(String rowKey) {
		hbaseGet = new org.apache.hadoop.hbase.client.Get(Bytes.toBytes(rowKey));
	}
	
	@Override
	public Get addFamily(String familyName) {
		hbaseGet.addFamily(Bytes.toBytes(familyName));
		return this;
	}

	@Override
	public Get addColumn(String columnFamily, String column) {
		hbaseGet.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(column));
		return this;
	}

}
