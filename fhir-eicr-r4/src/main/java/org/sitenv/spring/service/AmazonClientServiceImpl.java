package org.sitenv.spring.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

import org.sitenv.spring.model.MetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;

@Service("AmazonClientService")
public class AmazonClientServiceImpl implements AmazonClientService {

    private static final Logger logger = LoggerFactory.getLogger(AmazonClientServiceImpl.class);

    private S3Client s3Client;

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
        AwsBasicCredentials credentials = AwsBasicCredentials.create(this.accessKeyId, this.secretKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    // Upload Bundle to S3 Bucket
    @Override
    public String uploadBundle3bucket(String messageId, String xml)  {
        String s3Key = messageId + "/EICR_FHIR.xml"; // RequestId/EICR_FHIR.xml

        try (InputStream inputStream = new ByteArrayInputStream(xml.getBytes())) {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentLength((long) xml.getBytes().length)
                    .contentType("application/xml")
                    .build();

            // Convert InputStream to RequestBody
            RequestBody requestBody = RequestBody.fromInputStream(inputStream, xml.getBytes().length);

            PutObjectResponse response = s3Client.putObject(request, requestBody);
            logger.debug("Successfully uploaded to S3 {} / {}", bucketName, s3Key);
        } catch (S3Exception e) {
            logger.error("Error Message:    {}", e.getMessage());
            logger.error("HTTP Status Code: {}", e.statusCode());
            logger.error("AWS Error Code:   {}", e.awsErrorDetails().errorCode());
           // logger.error("Error Type:       {}", e.awsErrorDetails().errorType());
            logger.error("Request ID:       {}", e.requestId());
            return "Fail to upload Service Exception; messageId " + messageId + " Error Message: " + e.getMessage();
        } catch (SdkException e) {
            logger.error("Error Message:    {}", e.getMessage());
       
    
        } catch (IOException e) {
        	logger.error("Io Error Message:    {}", e.getMessage());
		}
        return "Successfully uploaded to S3 " + bucketName + "/" + s3Key;
    }

    // Upload MetaData to S3 Bucket
    public String uploadMetaDataS3bucket(String messageId, MetaData metaData) {
        String s3Key = messageId + "/MetaData.json";

        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonStr = mapper.writeValueAsString(metaData);

            try (InputStream inputStream = new ByteArrayInputStream(jsonStr.getBytes())) {
                PutObjectRequest request = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Key)
                        .contentType("application/json")
                        .contentLength((long) jsonStr.getBytes().length)
                        .build();

                // Convert InputStream to RequestBody
                RequestBody requestBody = RequestBody.fromInputStream(inputStream, jsonStr.getBytes().length);

                PutObjectResponse response = s3Client.putObject(request, requestBody);
                logger.debug("Successfully uploaded to S3 {} / {}", bucketName, s3Key);
            } catch (IOException e) {
            	logger.error("IOException Message:    {}", e.getMessage());
			}
        } catch (S3Exception e) {
            logger.error("Error Message:    {}", e.getMessage());
            logger.error("HTTP Status Code: {}", e.statusCode());
            logger.error("AWS Error Code:   {}", e.awsErrorDetails().errorCode());
          //  logger.error("Error Type:       {}", e.awsErrorDetails().errorType());
            logger.error("Request ID:       {}", e.requestId());
            return "Fail to upload Service Exception; messageId " + messageId + " Error Message: " + e.getMessage();
        } catch (SdkException e) {
            logger.error("Error Message:    {}", e.getMessage());
            return "Fail to upload Client Exception; messageId " + messageId + " Error Message: " + e.getMessage();
        } catch (JsonProcessingException e) {
            logger.error("Error Message:    {}", e.getMessage());
     
            return "Fail to Convert MetaData to JSON String; messageId " + messageId + " Error Message: " + e.getMessage();
        }
        return "Successfully uploaded MetaData to S3 " + bucketName + "/" + s3Key;
    }



	

}
