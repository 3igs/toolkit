package bigs.api.storage;

public interface Table {

	/**
	 * creates a Put object to be used when inserting data from the table
	 * @param key
	 * @return
	 */
	public Put createPutObject (String key);

	/**
	 * Inserts data in the table
	 * @param put
	 */
	public void put (Put put);
	

	/**
	 * creates a Get object to be used when updating data from the table
	 * @param key
	 * @return
	 */
	public Update createUpdateObject (String key);

	/**
	 * Updates data in the table
	 * @param put
	 */
	public void update (Update update);
	
	/**
	 * Atomically checks if a row/family/qualifier value matches the expected value. If it does, it adds the put. 
	 * If the passed value is null, the check is for the lack of column (ie: non-existance)
	 * @param row to check
	 * @param columnFamily column family to check
	 * @param columnName column qualifier to check
	 * @param value the expected value
	 * @param put data to put if check succeeds
	 * @return true if the new put was executed, false otherwise
	 */
	public boolean checkAndPut(String row, String columnFamily, String columnName, byte[] value, Put put);
	
	/**
	 * creates a Get object to be used when retrieving data from the table
	 * @param key
	 * @return
	 */
	public Get createGetObject (String key);
	
	/**
	 * extracts certain cells from a given row
	 * @param get
	 * @return
	 */
	public Result get (Get get);
	
	/**
	 * returns the names of the column families present in this table.
	 * If the underlying storage cannot now this in advance, it must return null.
	 * @return
	 */
	public String[] getColumnFamilies();
	
	/**
	 * creates a Scan objects to be used to define a scan of the table
	 * @return
	 */
	public Scan createScanObject();	
	
	/**
	 * Returns a scanner on the current table as specified by the Scan object.
	 * @return
	 */
	public ResultScanner getScan(Scan scan);
	
	/**
	 * Atomically increments a column value.
	 * @param row
	 * @param columnFamily
	 * @param columnName
	 * @param amount
	 * @return
	 */
	public long incrementColumnValue(String row, String columnFamily, String columnName, long amount);
	
}
