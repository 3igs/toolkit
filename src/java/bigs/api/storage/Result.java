package bigs.api.storage;

import java.util.Map;

public interface Result {
	
	/**
	 * returns the key corresponding to the row from which this Result object was created
	 * @return
	 */
	public String getRowKey();
	
	/**
	 * returns the cell value of the specified column
	 * @param columnFamily
	 * @param column
	 * @return
	 */
	public byte[] getValue(String columnFamily, String column);
	
	/**
	 * returns a map <columnName, columnValue> of the values contained in the columns of a column family
	 * of this result
	 * @param familyName
	 * @return
	 */
	public Map<String, String> getFamilyMap(String familyName);

}
