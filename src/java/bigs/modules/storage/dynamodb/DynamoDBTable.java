package bigs.modules.storage.dynamodb;


import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import bigs.api.exceptions.BIGSException;
import bigs.api.storage.Get;
import bigs.api.storage.Put;
import bigs.api.storage.Result;
import bigs.api.storage.ResultScanner;
import bigs.api.storage.Scan;
import bigs.api.storage.Table;
import bigs.api.storage.Update;

import com.amazonaws.services.dynamodb.model.AttributeAction;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodb.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodb.model.DescribeTableRequest;
import com.amazonaws.services.dynamodb.model.ExpectedAttributeValue;
import com.amazonaws.services.dynamodb.model.GetItemRequest;
import com.amazonaws.services.dynamodb.model.GetItemResult;
import com.amazonaws.services.dynamodb.model.Key;
import com.amazonaws.services.dynamodb.model.PutItemRequest;
import com.amazonaws.services.dynamodb.model.QueryRequest;
import com.amazonaws.services.dynamodb.model.QueryResult;
import com.amazonaws.services.dynamodb.model.ReturnValue;
import com.amazonaws.services.dynamodb.model.TableDescription;
import com.amazonaws.services.dynamodb.model.UpdateItemRequest;
import com.amazonaws.services.dynamodb.model.UpdateItemResult;


public class DynamoDBTable implements Table {

	String tableName = "";
	DynamoDBDataSource dataSource;
	TableDescription tableDescription = null;
	
	public DynamoDBTable(String tableName, DynamoDBDataSource dataSource) {
		this.tableName = tableName;
		this.dataSource = dataSource;
	}
	
	TableDescription getTableDescription() {
		if (tableDescription==null) {
			tableDescription = dataSource.dynamoDBclient.describeTable(
					  new DescribeTableRequest().withTableName(tableName)).getTable();		
		}
		return tableDescription;
	}
	
	@Override
	public Put createPutObject(String key) {
		return new DynamoDBPutUpdate(this, key);
	}
	
	@Override
	public Update createUpdateObject(String key) {
		return new DynamoDBPutUpdate(this, key);
	}

	@Override
	public void put(Put put) {
		if (! (put instanceof DynamoDBPutUpdate)) {
			throw new BIGSException ("framework is calling DynamoDB datasource implementation with non DynamoDB generated objects");
		}
		DynamoDBPutUpdate dput = (DynamoDBPutUpdate)put;
		
		// if it has content then store it first into its corresponding bucket and retrieve its URL
		if (dput.contentData!=null) {
			DynamoDBUtils.storeInS3(dataSource, tableName, dput.key, dput.contentData);
		}		
		
        PutItemRequest putItemRequest = new PutItemRequest(tableName, dput.getPutItem());
        dataSource.dynamoDBclient.putItem(putItemRequest);		
	}

	@Override
	public void update(Update update) {
		if (! (update instanceof DynamoDBPutUpdate)) {
			throw new BIGSException ("framework is calling DynamoDB datasource implementation with non DynamoDB generated objects");
		}
		DynamoDBPutUpdate dupdate = (DynamoDBPutUpdate)update;
		
		// if it has content then store it first into its corresponding bucket and retrieve its URL
		if (dupdate.contentData!=null) {
			DynamoDBUtils.storeInS3(dataSource, tableName, dupdate.key, dupdate.contentData);
		}		

		// prepares the request
		Key key = new Key().withHashKeyElement(new AttributeValue().withS(DynamoDBDataSource.noContent))
		           .withRangeKeyElement(new AttributeValue().withS(dupdate.key));
        UpdateItemRequest updateItemRequest = new UpdateItemRequest()
                            						.withTableName(tableName)
                            						.withKey(key)
                            						.withAttributeUpdates(dupdate.getUpdateItem());

        dataSource.dynamoDBclient.updateItem(updateItemRequest);		
	}
	@Override
	public boolean checkAndPut(String row, String columnFamily,
			String columnName, byte[] value, Put put) {
		if (! (put instanceof DynamoDBPutUpdate)) {
			throw new BIGSException ("framework is calling DynamoDB datasource implementation with non DynamoDB generated objects");
		}
		DynamoDBPutUpdate dput = (DynamoDBPutUpdate)put;
		
		// if it has content then store it first into its corresponding bucket and retrieve its URL
		if (dput.contentData!=null) {
			DynamoDBUtils.storeInS3(dataSource, tableName, dput.key, dput.contentData);
		}		
		
		// creates values conditions to check
		Map<String, ExpectedAttributeValue> expectedValues = new HashMap<String, ExpectedAttributeValue>();
		expectedValues.put(columnFamily+":"+columnName, 
						   new ExpectedAttributeValue()
							   .withValue(new AttributeValue().withS(new String(value))));
		

		// creates update object
		Map<String, AttributeValueUpdate> updateItems = new HashMap<String, AttributeValueUpdate>();
		for (String attr: dput.getPutItem().keySet()) {
			if (!attr.equals(DynamoDBDataSource.hashAttributeName) && !attr.equals(DynamoDBDataSource.rangeAttributeName)) {
				AttributeValue val = dput.getPutItem().get(attr);
				AttributeValueUpdate valUpdate = new AttributeValueUpdate(val, AttributeAction.PUT);
				updateItems.put(attr, valUpdate);
			}
		}
		
		// creates update request
		Key key = new Key().withHashKeyElement(new AttributeValue().withS(DynamoDBDataSource.noContent))
				           .withRangeKeyElement(new AttributeValue().withS(row));
		
		UpdateItemRequest updateItemRequest = 
				new UpdateItemRequest().withTableName(tableName)
									   .withKey(key)
									   .withExpected(expectedValues)
									   .withAttributeUpdates(updateItems)
									   .withReturnValues(ReturnValue.NONE);		
		
		try {
			dataSource.dynamoDBclient.updateItem(updateItemRequest);
		} catch (ConditionalCheckFailedException e) {
			return false;
		}
		
		return true;
    }

	@Override
	public Get createGetObject(String key) {
		return new DynamoDBGet(this, key);
	}

	@Override
	public Result get(Get get) {
		if (! (get instanceof DynamoDBGet)) {
			throw new BIGSException ("framework is calling DynamoDB datasource implementation with non DynamoDB generated objects");
		}
		
		DynamoDBGet dget = (DynamoDBGet)get;
		GetItemRequest getItemRequest = new GetItemRequest()
	    .withTableName(tableName)
	    .withKey(new Key().withHashKeyElement(new AttributeValue().withS(DynamoDBDataSource.noContent))
	    		          .withRangeKeyElement(new AttributeValue().withS(dget.key)))
	    .withConsistentRead(true);

		GetItemResult result = dataSource.dynamoDBclient.getItem(getItemRequest);
		return new DynamoDBResult(dataSource, this, result.getItem());
	}

	@Override
	/**
	 * there is no way in DynamoDB to know a-priori the fields of a db since they are dynamic.
	 * this method return null
	 */
	public String[] getColumnFamilies() {
		return null;
	}

	@Override
	public Scan createScanObject() {
		return new DynamoDBScan(this);
	}

	@Override
	public ResultScanner getScan(Scan scan) {
		if (! (scan instanceof DynamoDBScan)) {
			throw new BIGSException ("framework is calling DynamoDB datasource implementation with non DynamoDB generated objects");
		}
		DynamoDBScan dscan = (DynamoDBScan)scan;
		
		QueryRequest request = dscan.getRequest();
		QueryResult result = dataSource.dynamoDBclient.query(request);
		DynamoDBResultScanner drscanner = new DynamoDBResultScanner(dataSource, this, result, dscan);
		return drscanner;
		
	}

	@Override
	public long incrementColumnValue(String row, String columnFamily,
			String columnName, long amount) {
		
		String fullColumnName = columnFamily + ":" + columnName;
		
		Map<String, AttributeValueUpdate> updateItems = new HashMap<String, AttributeValueUpdate>();
		Key key = new Key().withHashKeyElement(new AttributeValue(DynamoDBDataSource.noContent))
				           .withRangeKeyElement(new AttributeValue(row));

		// Increments by one the value of the column
		updateItems.put(fullColumnName, 
		  new AttributeValueUpdate()
		    .withAction(AttributeAction.ADD)
		    .withValue(new AttributeValue().withN("+1")));		
		
		UpdateItemRequest updateItemRequest = new UpdateItemRequest()
		  .withTableName(tableName)
		  .withKey(key).withReturnValues(ReturnValue.UPDATED_NEW)
		  .withAttributeUpdates(updateItems);		
		UpdateItemResult result = dataSource.dynamoDBclient.updateItem(updateItemRequest);

		// and returns the resulting value
		AttributeValue val = result.getAttributes().get(fullColumnName);
		Long newValue = new Long(val.getN());
		return newValue;
	}

}
