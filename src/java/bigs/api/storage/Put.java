package bigs.api.storage;

/**
 * represents a row insert operation into the underlying storage
 * @author rlx
 *
 */
public interface Put {

	/**
	 * adds a cell value to the row represented by this object
	 * @param columnFamily
	 * @param column
	 * @param content
	 */
	public void add (String columnFamily, String columnName, byte[] value);
	
	/**
	 * adds a cell value forcing a given type to the row represented by this object.
	 * 
	 * @param columnFamily
	 * @param column
	 * @param value the object to store
	 */
	public void add (String columnFamily, String columnName, Object value);

}
