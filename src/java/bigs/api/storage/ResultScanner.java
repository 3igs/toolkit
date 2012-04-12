package bigs.api.storage;

/**
 * represents the result of a scan query in the underlying storage
 * @author rlx
 *
 */
public interface ResultScanner {

	/**
	 * Closes the scanner and releases any resources it has allocated
	 */
	public void close();
	
	/**
	 * Grab the next row's worth of values.
	 * @return
	 */
	public Result next();

	/**
	 * Grab the next 'nbRows' rows
	 * @param nbRows
	 * @return
	 */
	public Result[] next(int nbRows);
}
