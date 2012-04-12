package bigs.modules.storage.dynamodb;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.commons.io.IOUtils;


import bigs.api.exceptions.BIGSException;

import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

public class DynamoDBUtils {

	static AttributeValue valueFromObject(Object o) {

		AttributeValue r = null;
		if (o instanceof Integer ||
		    o instanceof Long ||
		    o instanceof Double ||
		    o instanceof Float ) r = new AttributeValue().withN(o.toString());
		
		if (o instanceof String) r = new AttributeValue(o.toString());
		
		if (o instanceof Boolean) {
			Boolean b = (Boolean)o;
			if (b) r = new AttributeValue("true");
			else r = new AttributeValue("false");
		}
		
		if (r!=null) return r;
		else {
			throw new BIGSException("unsupported type for DynamoDB: "+o.getClass().getCanonicalName());
		}
	}

	static void storeInS3(DynamoDBDataSource dataSource, String tableName, String key, byte[] contentData)  {
		storeInS3(dataSource, tableName, key, contentData, NO_OVERWRITE);
	}

	static Boolean NO_OVERWRITE = false;
	static Boolean FORCE_OVERWRITE = true;
	
	static void storeInS3(DynamoDBDataSource dataSource, String tableName, String key, byte[] contentData, Boolean overwrite)  {
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(contentData.length);
		PutObjectRequest request = new PutObjectRequest(dataSource.bucketName, 
													    tableName+"/"+key, 
													    new ByteArrayInputStream(contentData),
													    metadata);
		
		if (objectExistsInS3(dataSource, tableName, key) && !overwrite) {
				System.out.println("object "+tableName+"/"+key+" already exists in S3. not reloading it");
		} else {		
			dataSource.s3client.putObject(request);
		}
	}	
	
	static byte[] getFromS3(DynamoDBDataSource dataSource, String tableName, String key) {
		S3Object object = dataSource.s3client.getObject(
                new GetObjectRequest(dataSource.bucketName, 
              		  			   tableName+"/"+key));
		
		byte[] r;
		try {
			InputStream is = object.getObjectContent();
			r = IOUtils.toByteArray(is);
			is.close();
		} catch (IOException e) {
			throw new BIGSException("error processing byte array from dynamodb "+e.getMessage());
		}	
		return r;
	}
	
	static Date getLastModifiedDateFromS3 (DynamoDBDataSource dataSource, String tableName, String key) {
		S3Object object = dataSource.s3client.getObject(
                new GetObjectRequest(dataSource.bucketName, 
              		  			   tableName+"/"+key));
		ObjectMetadata metadata = object.getObjectMetadata();
		return metadata.getLastModified();
		
	}
	
	static Boolean objectExistsInS3(DynamoDBDataSource dataSource,  String tableName, String key) {
		try {
			ObjectMetadata object = dataSource.s3client.getObjectMetadata(new GetObjectMetadataRequest(dataSource.bucketName,  tableName+"/"+key));
			return true;
		} catch (AmazonS3Exception e) {
			if (e.getMessage().contains("Not Found")) {
				// this is ok, exception is signals that object does not exist
				return false;
			}
			throw e;
		} 
	}
}
