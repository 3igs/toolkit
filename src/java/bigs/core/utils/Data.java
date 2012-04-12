package bigs.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jmimemagic.Magic;

import org.apache.hadoop.hbase.util.Bytes;

import bigs.api.exceptions.BIGSException;
import bigs.api.storage.DataSource;
import bigs.api.storage.Get;
import bigs.api.storage.Put;
import bigs.api.storage.Result;
import bigs.api.storage.ResultScanner;
import bigs.api.storage.Scan;
import bigs.api.storage.Table;
import bigs.api.storage.Update;
import bigs.api.utils.TextUtils;
import bigs.core.BIGS;
import bigs.core.exceptions.BIGSTableExistsException;
import bigs.core.explorations.Evaluation;
import bigs.core.explorations.Exploration;


public class Data {
	
	public static String[] dataTableColumnFamilies = new String[] {"bigs", "content", "metadata", "splits"};
	/*
	public static Map<String, List<String>> dataTableColumnsPerFamily = new HashMap<String, List<String>>();
	
	static {
		dataTableColumnsPerFamily.put("bigs", new ArrayList<String>());
		dataTableColumnsPerFamily.put("content", new ArrayList<String>());
		dataTableColumnsPerFamily.put("metadata", new ArrayList<String>());
		dataTableColumnsPerFamily.put("splits", new ArrayList<String>());
		
		dataTableColumnsPerFamily.get("bigs").add("hostname");
		dataTableColumnsPerFamily.get("bigs").add("uuid");
		dataTableColumnsPerFamily.get("bigs").add("importDate");
		
		dataTableColumnsPerFamily.get("content").add("data");
		
		dataTableColumnsPerFamily.get("metadata").add("path");
		dataTableColumnsPerFamily.get("metadata").add("type");
		dataTableColumnsPerFamily.get("metadata").add("size");
	}
*/
	/**
	 * creates core bigs tables and field information in data source if the don't exist
	 * @param dataSource
	 */
	static public void initBIGSTables(DataSource dataSource) {
				
		Data.createTableIfDoesNotExist(dataSource, BIGS.tableName, BIGS.columnFamilies);
		Data.createTableIfDoesNotExist(dataSource, Evaluation.tableName, Evaluation.columnFamilies);
		Data.createTableIfDoesNotExist(dataSource, Exploration.tableName, Exploration.columnFamilies);		
	}
	
	
	static public void createDataTableIfDoesNotExist (DataSource dataSource, String tableName) {
		Data.createTableIfDoesNotExist(dataSource, tableName, dataTableColumnFamilies);
	}
	
	/**
	 * adds the contents of a file to a data table.
	 * Content is added to the column "content:data". Other metadata is added to the "metadata" family
	 * @param dataSource the object representing the data source 
	 * @param tableName the name of the data table
	 * @param file the file to add
	 */
	static public void uploadFile(File file, DataSource dataSource, String tableName) {
		if (!file.exists()) {
			throw new BIGSException("file "+file.getAbsolutePath()+" does not exist when uploading into table "+tableName);
		}
		try {
			Table table = dataSource.getTable(tableName);
			Put   put   = table.createPutObject(file.getName());
			byte[] fileContent = getBytesFromFile(file);
			put.add("content", "data", fileContent);
			put.add("metadata", "path", file.getAbsolutePath().getBytes());
			put.add("bigs", "hostname", Network.getHostName().getBytes());
			put.add("bigs", "importDate", TextUtils.FULLDATE.format(Calendar.getInstance().getTime()).getBytes());
			put.add("bigs", "uuid", Core.myUUID.getBytes());
			put.add("metadata", "size", new Long(fileContent.length).toString().getBytes());
			
			String mimeType = "unknown";
			try {
				mimeType = Magic.getMagicMatch(fileContent).getMimeType().toString();
			} catch (Exception e) {
				throw new BIGSException("");
			} catch (java.lang.NoClassDefFoundError ne) {
				// do nothing, we assume the library does not know the file
			}
			
			put.add("metadata", "type", mimeType.getBytes());
			table.put(put);
		} catch (IOException e) {
			throw new BIGSException("could not upload file "+file.getName()+", "+e.getMessage());
		}
		
	}

	
	/**
	 * creates a table in the specified data source. If the table already exists, it checks it has the
	 * same column families
	 * 
	 * @param dataSource
	 * @param tableName
	 * @param columnFamilies
	 */
	static public Table createTableIfDoesNotExist (DataSource dataSource, String tableName, String[] columnFamilies) {
		try {
			Table table = dataSource.createTable(tableName, columnFamilies);
			Log.info("table "+tableName+" created");
			
			// if table is counters table, initialize counters
			if (tableName.equals(BIGS.tableName)) {
				Put   put   = table.createPutObject("counters");
				put.add("content", "exploration", 0L);
				table.put(put);
			}
			return table;
		} catch (BIGSTableExistsException e) {
			// this is ok, check if column families are the same
			Table table = dataSource.getTable(tableName);
			String existingFamilies[] = table.getColumnFamilies();
			
			// checks both ways to make sure columnFamilies are the same as expected
			if (existingFamilies!=null) {
				Boolean error = false;
				for (String family: columnFamilies) if (!Data.contains(existingFamilies, family)) error=true;
				for (String family: existingFamilies) if (!Data.contains(columnFamilies, family)) error=true;
				if (error) {
					throw new BIGSException ("wrong columns in existing table '"+tableName+
							"'. Expecting "+Arrays.toString(columnFamilies)+" found "+Arrays.toString(existingFamilies));
				}			
			}
			Log.debug("table "+tableName+" already exists. skipping its creation");
			return table;
		}
	}
	
	/**
	 * Drops a table from the underlying storage with user confirmation
	 * @param dataSource the data source containing the table to drop
	 * @param tableName the name of the table to drop
	 */
	public static void dropTableWithUserConfirmation (DataSource dataSource, String tableName) {
		
		if (dataSource.existsTable(tableName)) {
            System.out.println("table "+tableName+"exists. Tests will empty it. Continue (Y/N)?\n");
            java.io.DataInputStream in = new java.io.DataInputStream(System.in);
            String aLine;
			try {
				aLine = in.readLine();
	            if (aLine.equalsIgnoreCase("y")) {
	            	dataSource.dropTable(tableName);
	            	Log.info("dropped table "+tableName);
	            } else {
	                throw new BIGSException ("Not deleting "+tableName +" table. Tests aborted");
	            }			
			} catch (IOException e) {
				throw new BIGSException("io error getting user input "+e.getMessage());
			}
		}
		
		
	}

	
	/**
	 * distributes the rows in the table into a number of splits. Split is done simply by marking 
	 * each row with the split to which it belongs in a column "bigs:split". Assumes the table has 
	 * a column family named "splits" where split information for each exploration is to be stored.
	 * 
	 * <b>NOTE</b>: this is done by assigning consecutive rows to consecutive splits. It might be
	 * more efficient to assign consecutive rows to the same split, this way the same split would be
	 * more compacted within contiguous region servers, reducing the need of workers to contact
	 * practically ALL region servers.
	 * 
	 * @param tableName
	 * @param numberOfSplits
	 */
	public static void markSplits (DataSource dataSource, String tableName, Integer numberOfSplits, String splitName) {
		Table table = dataSource.getTable(tableName);
		Scan scan = table.createScanObject();
		scan.addFamily("metadata");
		ResultScanner rs = table.getScan(scan);
		try {
			Integer split = 1;
			for (Result rr = rs.next(); rr!=null; rr = rs.next()) {					
				String key = rr.getRowKey();
		    	Update update = table.createUpdateObject(key);
		    	update.add("splits", splitName, split.toString().getBytes());
				split++;
		    	if (split>numberOfSplits) split = 1;
		    	table.update(update);
			}
		} finally {
			rs.close();
		}		
	}
	
    /**
     * checks if an array contains one specific object
     * @param array
     * @param object
     * @return
     */
    public static <T> Boolean contains (T[] array, T object) {
    	for (T item: array) {
    		if (item.equals(object)) return true;
    	}
    	return false;
    }
    
    /**
     * checks if a set contains one specific object
     * @param set
     * @param object
     * @return
     */
    public static <T> Boolean contains (Set<T> set, T object) {
    	for (T item: set) {
    		if (item.equals(object)) return true;
    	}
    	return false;
    }
    
    /**
     * gets the value of the bigs:uuid column of the specified rowKey and table
     * @param dataSource
     * @param table
     * @param rowKey
     * @return
     */
    public static String getUUID (DataSource dataSource, String tableName, String rowKey) {
		Table table = dataSource.getTable(tableName);

		Get   get   = table.createGetObject(rowKey);
		get.addColumn("bigs", "uuid");
		Result r = table.get(get);
		byte[] value = r.getValue("bigs", "uuid");
    	if (value==null) return null;
    	else return new String(value);    	
    }
    
    /**
     * checks if the value of the bigs:uuid column of the specified rokey/table
     * is equal to the one corresponding to this bigs process
     * 
     * @param dataSource
     * @param tableName
     * @param rowKey
     * @return
     */
    public static Boolean hasMyUUID (DataSource dataSource, String tableName, String rowKey) {
    	String uuid = Data.getUUID(dataSource, tableName, rowKey);    	
    	return uuid.equals(Core.myUUID);
    	
    }

    /**
     * reads the contents of a file into a byte array
     * @param file the input file
     * @return
     * @throws IOException
     */
    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }    

    /**
     * fills in hostname, uuid, etc. in a Put object. Assumes a column family named "bigs"
     * @param put
     * @return the same put object filled in
     */
    public static Put fillInHostMetadata(Put put) {
		put.add("bigs", "hostname", Network.getHostName().getBytes());
		put.add("bigs", "uuid", Core.myUUID.getBytes());
    	return put;
    }
}
