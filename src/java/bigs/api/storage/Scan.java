package bigs.api.storage;

/**
 * represents a scan operation in the underlying storage
 * @author rlx
 *
 */
public interface Scan {

	/**
	 * Sets the start row of the scan
	 * @param startRow
	 */
	public void setStartRow(String startRow);
	
	/**
	 * Sets the stop row of the scan
	 * @param stopRow
	 */
	public void setStopRow(String stopRow);

	/**
	 * Get the column from the specified family with the specified column
	 * @param columnFamily
	 * @param columnName
	 */
	public void addColumn(String columnFamily, String columnName);
	
	/**
	 *  Get all columns from the specified family
	 * @param columnFamily
	 */
	public void addFamily(String columnFamily);
	
	/**
	 * sets a filter for this scan to return only rows with a given value in the 
	 * specified column
	 * @param columnFamily
	 * @param columnName
	 * @param value
	 */
	public void setFilterByColumnValue(String columnFamily, String columnName, byte[] value);

}
