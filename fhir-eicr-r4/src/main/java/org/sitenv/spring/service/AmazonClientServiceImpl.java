package org.sitenv.spring.service;

import java.io.ByteArrayInputStream;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sitenv.spring.model.MetaData;
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

	//Upload Bundle to S3 Bucket
	public String uploadBundle3bucket(String messageId, String xml) {

		String s3Key = messageId + "/EICR_FHIR.xml"; // RequestId/EICR_FHIR.xml
		ObjectMetadata meta = new ObjectMetadata();
		meta.setContentLength(xml.getBytes().length);
		meta.setContentType("application/xml");

		try {
			s3client.putObject(bucketName, s3Key, new ByteArrayInputStream(xml.getBytes()), meta);
			logger.debug("Successfully uploaded to s3 " + bucketName + "/" + s3Key);
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
		return "Successfully uploaded to s3 " + bucketName + "/" + s3Key;
	}

	//Upload MetaData to S3 Bucket
	public String uploadMetaDataS3bucket(String messageId, MetaData metaData) {

		String s3Key = messageId + "/MetaData.json"; 
		ObjectMetadata meta = new ObjectMetadata();
		meta.setContentType("application/json");

		try {
			ObjectMapper mapper = new ObjectMapper();
			String jsonStr = mapper.writeValueAsString(metaData);

			s3client.putObject(bucketName, s3Key, new ByteArrayInputStream(jsonStr.getBytes()), meta);
			logger.debug("Successfully uploaded to s3 " + bucketName + "/" + s3Key);
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
		} catch (JsonProcessingException e) {
			logger.error("StackTrace:       " + e.getStackTrace());
			return "Fail to Convert MetaData to JSON String " + messageId + "Error Message: " + e.getMessage();
		}
		return "Successfully uploaded MetaData to s3 " + bucketName + "/" + s3Key;
	}

}
