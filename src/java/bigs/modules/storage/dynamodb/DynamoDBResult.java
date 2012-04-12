package bigs.modules.storage.dynamodb;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import bigs.api.exceptions.BIGSException;
import bigs.api.storage.Result;
import bigs.core.utils.Log;

import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;


public class DynamoDBResult implements Result {

	Map<String, AttributeValue> map ;
	DynamoDBDataSource dataSource;
	DynamoDBTable table;
	
	public DynamoDBResult (DynamoDBDataSource dataSource, DynamoDBTable table, Map<String, AttributeValue> map) {
		this.dataSource = dataSource;
		this.table = table;
		this.map = map;
	}
	
	
	@Override
	public String getRowKey() {
		return map.get(DynamoDBDataSource.rangeAttributeName).getS();
	}

	@Override
	public byte[] getValue(String columnFamily, String column) {
		
		// in case of content:data retrieve the content from s3
		if (columnFamily.equals("content") && column.equals("data")) {

			byte[] r = DynamoDBUtils.getFromS3(this.dataSource, this.table.tableName, this.getRowKey());			
			return r;
			
		} else {
			if (map==null) return null;
			String fullColumnName = columnFamily + ":" + column;		
			AttributeValue val = map.get(fullColumnName);	
			byte[] r;
			if (val==null) r = null;
			else if (val.getS()==null || val.getS().isEmpty() || val.getS().trim().isEmpty()) r = null;
			else r = val.getS().getBytes();
			return r;
		}
	}

	@Override
	public Map<String, String> getFamilyMap(String familyName) {
		Map<String, String> r = new HashMap<String, String>();
		
		for (String field: map.keySet()) {
			if (field.startsWith(familyName+":")) {
				String fieldName = field.substring(familyName.length()+1, field.length());
				String fieldValue = map.get(field).getS();
				r.put(fieldName, fieldValue);						
			}
			
		}
		return r;
	}

}
