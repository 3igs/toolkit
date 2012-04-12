package bigs.api.storage;

/**
 * represents a row udpate operation in the underlying storage
 * 
 * @author rlx
 *
 */
public interface Update  {

	/**
	 * adds a cell value to the row represented by this object
	 * @param columnFamily
	 * @param column
	 * @param content
	 */
	public void add (String columnFamily, String columnName, byte[] value);
	
}