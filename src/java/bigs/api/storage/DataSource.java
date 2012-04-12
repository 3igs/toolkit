package bigs.api.storage;

import java.util.Set;

import bigs.api.core.Configurable;
import bigs.core.exceptions.BIGSTableExistsException;


/**
 * fields requiring user configuration in any DataSource (classes implementing this interface)
 * must be annotated as @BIGSParam, this way the framework will look for them in
 * exploration or configuration files
 * 
 * @author rlx
 *
 */
public interface DataSource extends Configurable {

	/**
	 * Implementations of this method must initialize the data source (make connections, etc.)
	 * The framework will call this method AFTER filling in the fields annotated with @BIGSParam 
	 * with user provided data in.
	 */
	public void initialize();

	/**
	 * returns true if the connection and the datasource are working
	 * @return true if the datasource is working, false otherwise
	 */
	public Boolean isAlive();
	
	
	/**
	 * Gets a Table object representing a table in the underlying DB
	 * 
	 * @param tableName the name of the table to retrieve
	 * @return a Table object representing the underlying database table
	 */
	public Table getTable(String tableName);
	
	/**
	 * Creates a new table in the DB with the given column families
	 * 
	 * @param tableName the name of the table to create
	 * @param columnFamilies an array with the names of the column families of the new table
	 */
	public Table createTable(String tableName, String[] columnFamilies) throws BIGSTableExistsException;

	/**
	 * Drops a table in the DB with all its content
	 * 
	 * @param tableName the name of the table to drop
	 */
	public void dropTable(String tableName);
	
	/**
	 * returns true if the specified table exists in the database
	 * @param tableName the name of the table to check its exsistance
	 * @return true if the table exists in the database, false otherwise
	 */
	public Boolean existsTable(String tableName);
	
	/**
	 * returns the timestamp of this data source, used to get a reference timeframe for all
	 * @return
	 */
	public long getTime();
	
}
