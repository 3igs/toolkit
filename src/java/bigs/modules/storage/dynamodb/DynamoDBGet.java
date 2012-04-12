package bigs.modules.storage.dynamodb;



import bigs.api.storage.Get;

/**
 * In DynamoDB we always get all attributes, since the 64KB limit for row (item) size
 * is safe to avoid excessive traffic.
 * @author rlx
 *
 */
public class DynamoDBGet implements Get {

	String key;
	DynamoDBTable table;
	
	public DynamoDBGet (DynamoDBTable table, String key) {
		this.key = key;
		this.table = table;
	}
	@Override
	public Get addFamily(String familyName) {
		return this;
	}

	@Override
	public Get addColumn(String columnFamily, String columnName) {
		return this;
	}

}
