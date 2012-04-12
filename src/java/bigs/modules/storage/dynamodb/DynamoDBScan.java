package bigs.modules.storage.dynamodb;

import java.util.HashMap;
import java.util.Map;

import bigs.api.storage.Scan;

import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.ComparisonOperator;
import com.amazonaws.services.dynamodb.model.Condition;
import com.amazonaws.services.dynamodb.model.QueryRequest;


/**
 * scans are done through DynamoDB queries, not through DynamoDB scans, since a DynamoDB scan
 * ALWAYS retrieves the entire table.
 * @author rlx
 *
 */
public class DynamoDBScan implements Scan {

	private DynamoDBTable table;
	private QueryRequest request;
	private String startRow = null;
	private String stopRow = null;
	
	private Map<String, String> filters = new HashMap<String, String>();
	
	
	public DynamoDBScan(DynamoDBTable table) {
		this.table = table;
		this.request = new QueryRequest().withTableName(table.tableName).withLimit(4);
	}
	
	public QueryRequest getRequest() {
	    Condition condition = null;
		if (startRow!=null && stopRow==null) {
			condition = new Condition().withComparisonOperator(ComparisonOperator.GE)
					                                .withAttributeValueList(new AttributeValue().withS(startRow));
		} else if (startRow==null && stopRow!=null) {
			condition = new Condition().withComparisonOperator(ComparisonOperator.LE)
                    .withAttributeValueList(new AttributeValue().withS(stopRow));			
		} else if (startRow!=null && stopRow!=null) {
			condition = new Condition().withComparisonOperator(ComparisonOperator.BETWEEN)
                    .withAttributeValueList(new AttributeValue().withS(startRow), new AttributeValue().withS(stopRow));			
		}
		request.setRangeKeyCondition(condition);
		request.withHashKeyValue(new AttributeValue().withS(DynamoDBDataSource.noContent));
		
		return request;
	}
	
	DynamoDBTable getTable() {
		return table;
	}
	
	Map<String, String> getFilters() {
		return filters;
	}
	
	@Override
	public void setStartRow(String startRow) {
		this.startRow = startRow;
	}

	@Override
	public void setStopRow(String stopRow) {
		this.stopRow = stopRow;
	}

	@Override
	public void addColumn(String columnFamily, String columnName) {
	}

	@Override
	public void addFamily(String columnFamily) {
	}

	@Override
	public void setFilterByColumnValue(String columnFamily, String columnName,
			byte[] value) {

		filters.put(columnFamily+":"+columnName, new String(value));
	}

}
