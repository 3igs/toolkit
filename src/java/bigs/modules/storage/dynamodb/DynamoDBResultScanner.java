package bigs.modules.storage.dynamodb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import bigs.api.storage.Result;
import bigs.api.storage.ResultScanner;
import bigs.core.utils.Log;

import com.amazonaws.services.dynamodb.model.Key;
import com.amazonaws.services.dynamodb.model.QueryRequest;
import com.amazonaws.services.dynamodb.model.QueryResult;
import com.amazonaws.services.dynamodb.model.AttributeValue;


public class DynamoDBResultScanner implements ResultScanner {

	QueryResult dqueryResult;
	DynamoDBScan scan;
	DynamoDBDataSource dataSource;
	DynamoDBTable table;
	
	List<Map<String,AttributeValue>> currentItems = null;
	Integer currentItemNumber = 0;
	Key lastKey = null;
	
	public DynamoDBResultScanner(DynamoDBDataSource dataSource, DynamoDBTable table, QueryResult dqueryResult, DynamoDBScan scan) {
		this.dqueryResult = dqueryResult;
		this.scan = scan;
		this.currentItemNumber = 0;
		this.currentItems = dqueryResult.getItems();
		this.lastKey = dqueryResult.getLastEvaluatedKey();
		this.dataSource = dataSource;
		this.table = table;
	}
	
	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Result next() {
		if (currentItemNumber >= currentItems.size()) {
			// in case there are no more keys
			if (lastKey==null) return null;
			QueryRequest originalRequest = scan.getRequest();
			QueryRequest newQueryRequest = new QueryRequest()
			      .withAttributesToGet(originalRequest.getAttributesToGet())
			      .withCount(originalRequest.getCount())
			      .withLimit(originalRequest.getLimit())
			      .withRangeKeyCondition(originalRequest.getRangeKeyCondition())
			      .withHashKeyValue(originalRequest.getHashKeyValue())
			      .withTableName(originalRequest.getTableName())
			      .withExclusiveStartKey(lastKey);
			currentItemNumber = 0;
			QueryResult sresult = scan.getTable().dataSource.dynamoDBclient.query(newQueryRequest);
			currentItems = sresult.getItems();
			lastKey = sresult.getLastEvaluatedKey();
		}

		if (currentItems.size()==0) return null;
		Map<String,AttributeValue> currentItem = currentItems.get(currentItemNumber);
		DynamoDBResult r = new DynamoDBResult(dataSource, table, currentItems.get(currentItemNumber));
		currentItemNumber++;
		
		// check filter conditions
		Map<String, String> filters = scan.getFilters();
		Boolean matches = true;
		for (String s : filters.keySet()) {
			AttributeValue val = currentItem.get(s);
			if (val!=null) {
				String value = val.getS();
				if (!value.equals(filters.get(s))) {
					matches = false;
					break;
				}
			}
		}
		
		// if it does not match the filter, the next one is returned.
		if (matches) return r;
		else return next();
	}

	@Override
	public Result[] next(int nbRows) {
		List<Result> r = new ArrayList<Result>();
		
		for (int i=0; i<nbRows; i++) {
			Result result = this.next();
			if (result==null) break;
			r.add(result);
		}
		
		Result[] ra = new Result[r.size()];
		
		for (int i=0; i<r.size(); i++) ra[i] = r.get(i);
		return ra;
	}

}
