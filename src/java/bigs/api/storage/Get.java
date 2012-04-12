package bigs.api.storage;

/**
 * represents a row retrieval operation in the underlying storage
 * @author rlx
 *
 */
public interface Get {

	/**
	 * gets all columns from the specified family
	 * @param familyName
	 * @return the Get object
	 */
	public Get addFamily (String familyName);
	
	/**
	 * gets the specified column within the specified family
	 * @param columnFamily
	 * @param column
	 * @return the Get object
	 */
	public Get addColumn (String columnFamily, String columnName);
}
