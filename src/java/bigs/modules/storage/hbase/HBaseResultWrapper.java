package bigs.modules.storage.hbase;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;

import org.apache.hadoop.hbase.util.Bytes;

import bigs.api.storage.Result;


public class HBaseResultWrapper implements Result {

	org.apache.hadoop.hbase.client.Result hbaseResult;
	
	public HBaseResultWrapper (org.apache.hadoop.hbase.client.Result hbaseResult) {
		this.hbaseResult = hbaseResult;
	}
	
	@Override
	public String getRowKey() {
		return new String(hbaseResult.getRow());
	}

	@Override
	public byte[] getValue(String columnFamily, String column) {
		return hbaseResult.getValue(Bytes.toBytes(columnFamily), Bytes.toBytes(column));
	}

	@Override
	public Map<String, String> getFamilyMap(String familyName) {
		Map<String, String> r = new HashMap<String, String>();
		NavigableMap<byte[], byte[]> m = hbaseResult.getFamilyMap(Bytes.toBytes(familyName));
		
		for (byte[] key: m.keySet()) {
			String keyAsString = new String(key);
			String valueAsString = new String(m.get(key));
			r.put(keyAsString, valueAsString);			
		}
		
		return r;
	}
	
}
