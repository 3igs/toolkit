package bigs.modules.storage.dynamodb;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.util.log.Log;



import bigs.api.exceptions.BIGSException;
import bigs.api.storage.Put;
import bigs.api.storage.Update;

import com.amazonaws.services.dynamodb.model.AttributeAction;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.AttributeValueUpdate;

public class DynamoDBPutUpdate implements Put, Update {

	String key;
	Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
	DynamoDBTable table;
	
	byte[] contentData = null;
	
	public DynamoDBPutUpdate(DynamoDBTable table, String key) {
		this.key = key;
		this.table = table;
		item.put(DynamoDBDataSource.rangeAttributeName, new AttributeValue(key));	
		item.put(DynamoDBDataSource.hashAttributeName, new AttributeValue(DynamoDBDataSource.noContent));
	}
	
	@Override
	public void add(String columnFamily, String columnName, byte[] value) {
		
		if (columnFamily.equals("content") && columnName.equals("data")) {
			contentData = value;
		} else {			
			String svalue = new String(value);
			if (svalue==null || svalue.isEmpty()) svalue=" "; 
			item.put(columnFamily+":"+columnName, new AttributeValue(svalue));
		}
	}

	Map<String, AttributeValue> getPutItem() {
		return item;
	}

	Map<String, AttributeValueUpdate> getUpdateItem() {
		// includes all attributes except the rowkey (which cannot be updated)
		Map<String, AttributeValueUpdate> r = new HashMap<String, AttributeValueUpdate>();
		for (String attrName : item.keySet()) {
			if (!attrName.equals(DynamoDBDataSource.hashAttributeName) && !attrName.equals(DynamoDBDataSource.rangeAttributeName)) {
				AttributeValue val = item.get(attrName);
				AttributeValueUpdate update = new AttributeValueUpdate().withValue(val).withAction(AttributeAction.PUT);
				r.put(attrName, update);
			}
		}
		return r;
	}
	
	@Override
	public void add(String columnFamily, String columnName, Object value) {
		if (columnFamily.equals("content") && columnName.equals("data")) {
			throw new BIGSException ("internal BIGS error, content:data can only contain byte[] ");
		}
		item.put(columnFamily+":"+columnName, DynamoDBUtils.valueFromObject(value));
	}
}
