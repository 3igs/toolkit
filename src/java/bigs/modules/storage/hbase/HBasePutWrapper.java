package bigs.modules.storage.hbase;

import org.apache.hadoop.hbase.util.Bytes;

import bigs.api.storage.Put;
import bigs.api.storage.Update;


public class HBasePutWrapper implements Put, Update {

	org.apache.hadoop.hbase.client.Put hbasePut;
	
	public HBasePutWrapper (String rowKey) {
		hbasePut = new org.apache.hadoop.hbase.client.Put(Bytes.toBytes(rowKey));
	}
	
	@Override
	public void add(String columnFamily, String columnName, byte[] value) {
		hbasePut.add(Bytes.toBytes(columnFamily), Bytes.toBytes(columnName), value);
	}

	@Override
	public void add(String columnFamily, String columnName, Object value) {
		byte[] byteValue = null;
		if (value instanceof Integer) byteValue = Bytes.toBytes((Integer)value);
		if (value instanceof Long) byteValue = Bytes.toBytes((Long)value);
		if (value instanceof Double) byteValue = Bytes.toBytes((Double)value);
		if (value instanceof Short) byteValue = Bytes.toBytes((Short)value);
		if (value instanceof String) byteValue = Bytes.toBytes((String)value);
		if (value instanceof Float) byteValue = Bytes.toBytes((Float)value);
		if (value instanceof Boolean) byteValue = Bytes.toBytes((Boolean)value);
		
		this.add(columnFamily, columnName, byteValue);		
	}

}
