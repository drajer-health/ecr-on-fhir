package com.drajer.eicrresponder.service.impl;

import java.io.ByteArrayInputStream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.drajer.eicrresponder.service.AmazonClientService;

@Service("AmazonClientService")
public class AmazonClientServiceImpl implements AmazonClientService {

	private static final Logger logger = LoggerFactory.getLogger(AmazonClientServiceImpl.class);

	private AmazonS3 s3client;

	// @Value("${s3.endpointUrl}")
	// private String endpointUrl;

	@Value("${s3.bucketName}")
	private String bucketName;

	@Value("${s3.accessKeyId}")
	private String accessKeyId;

	@Value("${s3.secretKey}")
	private String secretKey;

	@Value("${s3.region}")
	private String region;

	@PostConstruct
	private void initializeAmazon() {
		AWSCredentials credentials = new BasicAWSCredentials(this.accessKeyId, this.secretKey);
		this.s3client = AmazonS3ClientBuilder.standard().withRegion(region)
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
	}

	public String uploads3bucket(String messageId, String xml) {
		logger.info("in uploads3bucket:    " );
		String s3Key = messageId ; // RequestId/EICR_FHIR.json
		ObjectMetadata meta = new ObjectMetadata();
		meta.setContentLength(xml.getBytes().length);
		meta.setContentType("application/json");
		
		try {
			logger.info("uploads3bucket folderName:    " +bucketName);
			s3client.putObject(bucketName, s3Key, new ByteArrayInputStream(xml.getBytes()), meta);
			logger.debug("Successfully uploaded to s3 " + bucketName  + s3Key);
		} catch (AmazonServiceException ase) {
			logger.error("Error Message:    " + ase.getMessage());
			logger.error("HTTP Status Code: " + ase.getStatusCode());
			logger.error("AWS Error Code:   " + ase.getErrorCode());
			logger.error("Error Type:       " + ase.getErrorType());
			logger.error("Request ID:       " + ase.getRequestId());
			return "Fail to upload Service Exception; messageId " + messageId + "Error Message: " + ase.getMessage();
		} catch (AmazonClientException ace) {
			logger.error("Error Message:    " + ace.getMessage());
			logger.error("StackTrace:       " + ace.getStackTrace());
			return "Fail to upload Client Exception; messageId " + messageId + "Error Message: " + ace.getMessage();
		}
		return "Successfully uploaded to s3 " + bucketName + s3Key;
	}

}
