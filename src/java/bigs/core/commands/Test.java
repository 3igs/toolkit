package bigs.core.commands;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import pilot.core.PipelineStage;
import pilot.core.ScheduleItem;

import bigs.api.core.Algorithm;
import bigs.api.exceptions.BIGSException;
import bigs.api.storage.DataSource;
import bigs.api.storage.Get;
import bigs.api.storage.Put;
import bigs.api.storage.Result;
import bigs.api.storage.ResultScanner;
import bigs.api.storage.Scan;
import bigs.api.storage.Table;
import bigs.core.BIGS;
import bigs.core.explorations.Evaluation;
import bigs.core.explorations.Exploration;
import bigs.core.explorations.ExplorationStage;
import bigs.core.utils.Log;
import bigs.modules.storage.dynamodb.DynamoDBDataSource;

import com.amazonaws.services.dynamodb.model.Key;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodb.model.CreateTableRequest;
import com.amazonaws.services.dynamodb.model.KeySchema;
import com.amazonaws.services.dynamodb.model.KeySchemaElement;
import com.amazonaws.services.dynamodb.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodb.model.PutItemRequest;
import com.amazonaws.services.dynamodb.model.PutItemResult;
import com.amazonaws.services.dynamodb.model.ScanRequest;
import com.amazonaws.services.dynamodb.model.ScanResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;


public class Test extends Command {

	@Override
	public String getCommandName() {
		return "test";
	}

	@Override
	public String[] getHelpString() {
		return new String[] {
			"command for testing stuff"
		};
	}

    @Override
    public Boolean checkCallingSyntax(String[] args) {
    	return true;
    }

    @Override
	public void run(String[] args) throws Exception {
    	testPipelineProperties(args);
    }
    
    
    void testPipelineProperties(String[] args) throws Exception {
    	Properties props = new Properties();
    	props.load(new FileReader(new File(args[0])));
    	
    	PipelineStage p = PipelineStage.fromProperties(props, 1);    
    	
    	p.printOut();
System.out.println();    	
System.out.println();    	
    	List<ScheduleItem> schedule = p.list();
System.out.println();    	
System.out.println();    	
		for (ScheduleItem si: schedule) {
			System.out.println(si.toString());
		}
    	
    }

    
    void testDynamo(String[] args) throws Exception {
    	if (args.length==0) throw new BIGSException("must specify extra args");
    	DynamoDBDataSource d = (DynamoDBDataSource)BIGS.globalProperties.getConfiguredDataSource();
    	String tableName = "test-table";
    	
    	if (args[0].equals("create.table")) {
			KeySchema keySchema = new KeySchema()
			.withHashKeyElement(new KeySchemaElement().withAttributeName("hkey").withAttributeType("S"))
			.withRangeKeyElement(new KeySchemaElement().withAttributeName("rkey").withAttributeType("S"));

	        CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
	                .withKeySchema(keySchema)
	                .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(3L).withWriteCapacityUnits(5L));
	        d.getAmazonDynamoDBClient().createTable(createTableRequest);
    		
    	} else if (args[0].equals("fill.table")) {
    		for (Integer hkey=1; hkey<20; hkey++) {
    			for (Integer rkey=1; rkey<20; rkey++) {
    				Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
    				item.put("hkey", new AttributeValue().withS(hkey.toString()));
    				item.put("rkey", new AttributeValue().withS(rkey.toString()));
    				item.put("field1", new AttributeValue().withS(new Date().toString()));
    				item.put("field2", new AttributeValue().withS(new Date().toString()));
    				PutItemRequest putItemRequest = new PutItemRequest()
    				  .withTableName(tableName)
    				  .withItem(item);
    				PutItemResult result = d.getAmazonDynamoDBClient().putItem(putItemRequest);
    				Log.info("put "+result.toString());
    			}
    			
    		}
    	} else if (args[0].equals("scan")) {

	    	ScanResult result;

	    	Key lastKey = null;
	    	int i=1;
	    	do {
	    		ScanRequest scanRequest = new ScanRequest()
	    			.withTableName(tableName)
	    			.withLimit(1000);
		    	if (lastKey!=null) scanRequest.withExclusiveStartKey(lastKey);
		    	result = d.getAmazonDynamoDBClient().scan(scanRequest);
		    	for (Map<String, AttributeValue> item : result.getItems()) {
	    			StringBuffer sb = new StringBuffer();
		    		for (String fieldName: item.keySet()) {
		    			sb.append(fieldName).append("=").append(item.get(fieldName).getS()).append(" ");
		    		}	    		
		    		Log.info(i+"  "+sb.toString());
		    		i++;
		    	}  
		    	lastKey = result.getLastEvaluatedKey();
	    	} while (lastKey!=null);
    	}
    }
    
    void testEvals(String[] args) throws Exception {
    	String start = null;
    	String stop = null;
    	if (args.length>0)  start = args[0];
    	if (args.length>1)  stop = args[1];
    	
    	DataSource d = BIGS.globalProperties.getConfiguredDataSource();
    	
    	Table t = d.getTable(Evaluation.tableName);
    	Scan s = t.createScanObject();
    	if (start!=null) s.setStartRow(start);
    	if (stop!=null) s.setStopRow(stop);
    	ResultScanner rs = t.getScan(s);
    	Result r;
    	while ( (r=rs.next())!=null) {
    		Evaluation e = Evaluation.fromResultObject(r);
    		Log.info("-> "+e.toString());
    	}
    	
    }
    

    
    void testS3(String[] args) {
       String accessKey = "AKIAIOWFQRUK2AT6FPNA";    // example accessKey
       String secretKey = "EFz4K0HCSV8zcDWgj6GBlwdbH9C/nt4mvs46OQaL";    // example secretKey
       AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
       AmazonS3 s3 = 
           new AmazonS3Client(credentials, 
        		   new ClientConfiguration().withProtocol(Protocol.HTTP));
       
       String bucketName = "rramos";    // example bucketName
       String key = "folder/";
       Log.debug("creating folder in bucket");
       InputStream input = new ByteArrayInputStream(new byte[0]);
       ObjectMetadata metadata = new ObjectMetadata();
       metadata.setContentLength(0);
       s3.putObject(new PutObjectRequest(bucketName, key, input, metadata));    	
       Log.debug("done");
       
       
    }

    void testExplorations(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    	DataSource d = BIGS.globalProperties.getConfiguredDataSource();
    	
    	Table table = d.getTable(Exploration.tableName);
    	Scan scan  = table.createScanObject();    	
    	scan.setStartRow("00002");
    	scan.setStopRow("00007");
    	scan.setFilterByColumnValue("bigs", "status", "NEW".getBytes());
    	ResultScanner rs = table.getScan(scan);
    	Result r = null;
    	while ( (r=rs.next())!=null) {
    		Exploration ex = Exploration.fromResultObject(r);
    		System.out.println("retrieved exploration "+ex.getExplorationNumber()+" "+ex.getStatusAsString());
    	}
    	
    }
    
    void test01(String[] args) throws Exception {
		Log.info("testing stuff");
		
		File props = new File(args[0]);
		Log.info("using properties file at "+props.toString());
		
		Exploration expl = new Exploration();
		expl.load(new FileReader(props));
		
		List<ExplorationStage> stages = expl.getStages();
		for (ExplorationStage stage: stages) {
			System.out.println("----> stage "+stage.toString());
			for (Algorithm f: stage.getConfiguredAlgorithmList()) {
				System.out.println("    generated fe configuration "+f.toString());
			}
		}

				
		// storage sample generic usage
		DataSource dataSource = stages.get(0).getConfiguredOriginDataSource();
		Table table = dataSource.getTable("test");

		Get   get   = table.createGetObject("fe43");
		get.addColumn("content", "raw");
		Result r = table.get(get);
		byte[] rawImage = r.getValue("content", "raw");
		if (rawImage!=null) {
			String s = new String(rawImage);
			Log.info("got value "+s);
		} else {
			Log.info("no value for fe43");
		}
		
		
		Put   put   = table.createPutObject("config5");
		put.add("content", "lowPass", "123.21 923.1 87.2 3.3".getBytes());
		put.add("meta", "type", "config".getBytes());
		table.put(put);
		Log.info("just put image");
		
		Scan scan = table.createScanObject();
//		scan.addColumn("content", "lowPass");
		ResultScanner rs = table.getScan(scan);
		try {
			for (Result rr = rs.next(); rr!=null; rr = rs.next()) {
				String value = new String(rr.getValue("content", "lowPass"));
				Log.info("row "+rr.getRowKey());
				Log.info("value "+value);
			}
		} finally {
			rs.close();
		}
	}

	@Override
	public String getDescription() {
		return "command for testing stuff";
		
	}

}
