package bigs.modules.storage.dynamodb;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import bigs.api.core.BIGSParam;
import bigs.api.exceptions.BIGSException;
import bigs.api.storage.DataSource;
import bigs.api.storage.Table;
import bigs.core.BIGS;
import bigs.core.exceptions.BIGSTableExistsException;
import bigs.core.explorations.Exploration;
import bigs.core.utils.Log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodb.model.CreateTableRequest;
import com.amazonaws.services.dynamodb.model.DeleteTableRequest;
import com.amazonaws.services.dynamodb.model.DescribeTableRequest;
import com.amazonaws.services.dynamodb.model.KeySchema;
import com.amazonaws.services.dynamodb.model.KeySchemaElement;
import com.amazonaws.services.dynamodb.model.ListTablesResult;
import com.amazonaws.services.dynamodb.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodb.model.TableDescription;
import com.amazonaws.services.dynamodb.model.TableStatus;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.Region;


public class DynamoDBDataSource implements DataSource {

	@BIGSParam
	public String accessKey;
	
	@BIGSParam
	public String secretKey;
	
	@BIGSParam(isMandatory=false, description="bucket name to store content for data tables")
	public String bucketName = "bigs";
	
	@BIGSParam(isMandatory=false)
	public String bucketRegion = Region.US_Standard.toString();
	
	@BIGSParam(isMandatory=false)
	public String proxyHost;
	
	@BIGSParam(isMandatory=false)
	public Integer proxyPort;
	
	@BIGSParam(isMandatory=false)
	public String proxyUsername;
	
	@BIGSParam(isMandatory=false)
	public String proxyPassword;
	
	@BIGSParam(isMandatory=false, description="read capacity units for new tables")
	public Long readCapacity = 100L;

	@BIGSParam(isMandatory=false, description="write capacity units for new tables")
	public Long writeCapacity = 100L;
	
	@BIGSParam(isMandatory=false)
	public String dbEndPoint = "http://dynamodb.us-east-1.amazonaws.com/";
	
	AmazonDynamoDBClient dynamoDBclient;
    AmazonS3 s3client;
    Region s3region;
	
    static Region[] allowedRegions = new Region[] { Region.AP_Singapore, Region.AP_Tokyo, 
    												Region.EU_Ireland,
    												Region.SA_SaoPaulo,
    												Region.US_Standard, Region.US_West, Region.US_West_2 };
    
	static String hashAttributeName = "none";
	static String rangeAttributeName = "rowkey";
	static String noContent = "none";
	
	@Override
	public void initialize() {	
		
		// first check if the region was specified correctly
		try {
			s3region = Region.fromValue(bucketRegion);
		} catch (IllegalArgumentException e) {
			s3region = null;
		}
		if (s3region==null) {
			throw new BIGSException ("not allowed region for bucket "+bucketRegion);
		}

		// the create client objects
		AWSCredentials credentials = new BasicAWSCredentials (accessKey, secretKey);
		
		ClientConfiguration conf = null;
		if (proxyHost!=null) {
			conf = new ClientConfiguration();
			conf.setProxyHost(proxyHost);
			if (proxyPort!=null) conf.setProxyPort(proxyPort);
			if (proxyUsername!=null) conf.setProxyUsername(proxyUsername);
			if (proxyPassword!=null) conf.setProxyPassword(proxyPassword);
		}
		
		if (conf!=null) {
			dynamoDBclient = new AmazonDynamoDBClient(credentials, conf);
		    s3client = new AmazonS3Client(credentials, conf);
		} else {
			dynamoDBclient = new AmazonDynamoDBClient(credentials);
		    s3client = new AmazonS3Client(credentials);
		}
		dynamoDBclient.setEndpoint(dbEndPoint);
	}
	
	public AmazonDynamoDBClient getAmazonDynamoDBClient() {
		return dynamoDBclient;
	}

	@Override
	public Boolean isAlive() {
		
		try {
			// try a simple operation, if no exception we assume db is working
			dynamoDBclient.describeTable(new DescribeTableRequest().withTableName(BIGS.tableName));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public Table getTable(String tableName) {
		return new DynamoDBTable(tableName, this);
	}

	@Override
	public Table createTable(String tableName, String[] columnFamilies)
			throws BIGSTableExistsException {
		
		try {
			// creates the table
			KeySchema keySchema = new KeySchema()
				.withHashKeyElement(new KeySchemaElement().withAttributeName(hashAttributeName).withAttributeType("S"))
				.withRangeKeyElement(new KeySchemaElement().withAttributeName(rangeAttributeName).withAttributeType("S"));

			Long myReadCapacity = this.readCapacity;
			Long myWriteCapacity = this.writeCapacity;
			if (tableName.equals(BIGS.tableName) || tableName.equals(Exploration.tableName)) {
				myReadCapacity = 10L;
				myWriteCapacity = 10L;
			}
			
	        CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
	                .withKeySchema(keySchema)
	                .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(myReadCapacity).withWriteCapacityUnits(myWriteCapacity));
	        dynamoDBclient.createTable(createTableRequest);
	        
	        // creates the associated bucket
	        if (!s3client.doesBucketExist(bucketName)) {
	        	s3client.createBucket(bucketName, s3region);
	        }
	        
	        // creates a folder with the table name within the bucket
	        InputStream input = new ByteArrayInputStream(new byte[0]);
	        ObjectMetadata metadata = new ObjectMetadata();
	        metadata.setContentLength(0);
	        s3client.putObject(new PutObjectRequest(bucketName, tableName+"/", input, metadata));    	
	        
	        
		} catch (AmazonServiceException e) {
			if (e.getMessage().contains("Duplicate table name")) {
				throw new BIGSTableExistsException("table "+tableName+" already exists.");				
			} else {
				throw new BIGSException ("aws exception creating table "+e.getMessage());				
			}
		}
            
        this.waitForTableToBecomeAvailable(tableName);
        return this.getTable(tableName);
	}
	
    private void waitForTableToBecomeAvailable(String tableName) {
        System.out.println("Waiting for table " + tableName + " to become ACTIVE...");

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (10 * 60 * 1000);
        while (System.currentTimeMillis() < endTime) {
            try {Thread.sleep(1000 * 20);} catch (Exception e) {}
            try {
                DescribeTableRequest request = new DescribeTableRequest().withTableName(tableName);
                TableDescription tableDescription = dynamoDBclient.describeTable(request).getTable();
                String tableStatus = tableDescription.getTableStatus();
                System.out.println("  - current state: " + tableStatus);
                if (tableStatus.equals(TableStatus.ACTIVE.toString())) return;
            } catch (AmazonServiceException ase) {
                if (ase.getErrorCode().equalsIgnoreCase("ResourceNotFoundException") == false) throw ase;
            }
        }

        throw new RuntimeException("Table " + tableName + " never went active");
    }
	@Override
	public void dropTable(String tableName) {
		DeleteTableRequest req = new DeleteTableRequest(tableName);
		dynamoDBclient.deleteTable(req);
	}

	@Override
	public Boolean existsTable(String tableName) {
		List<String> list = dynamoDBclient.listTables().getTableNames();		
		return list.contains(tableName);		
	}

	
	/**
	 * gets time by putting a dummy object in s3 and retrieving its last modification date.
	 */	
	@Override	
	public long getTime() {
		String timeStampFile = "timestamp-file.dummy";
		DynamoDBUtils.storeInS3(this, BIGS.tableName, timeStampFile, "".getBytes(), DynamoDBUtils.FORCE_OVERWRITE);			
		Date lastModified = DynamoDBUtils.getLastModifiedDateFromS3(this, BIGS.tableName, timeStampFile);

		return lastModified.getTime();

	}

}
