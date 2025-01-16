package org.sitenv.spring.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

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
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;

/**
 * Service implementation for handling AWS S3 operations.
 * This service provides functionality to upload FHIR bundles and metadata to an S3 bucket.
 */
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

    /**
     * Initializes the Amazon S3 client after the service is constructed.
     */
    @PostConstruct
    private void initializeAmazon() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(this.accessKeyId, this.secretKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    /**
     * Uploads a FHIR bundle as an XML file to the S3 bucket.
     *
     * @param persistenceId The unique ID to be used in the S3 key.
     * @param xml           The FHIR bundle content in XML format.
     * @return A success or failure message indicating the result of the upload.
     */
    @Override
    public String uploadBundle3bucket(String persistenceId, String xml) {
        String s3Key = generateS3Key("RawFHIR-T-PH-ECR", persistenceId);
        return uploadToS3(s3Key, xml, "application/xml");
    }

    /**
     * Uploads metadata as a JSON file to the S3 bucket.
     *
     * @param persistenceId The unique ID to be used in the S3 key.
     * @param metaData      The metadata object to upload.
     * @return A success or failure message indicating the result of the upload.
     */
    public String uploadMetaDataS3bucket(String persistenceId, MetaData metaData) {
        try {
            String jsonStr = new ObjectMapper().writeValueAsString(metaData);
            String s3Key = generateS3Key("MetadataV2", persistenceId);
            return uploadToS3(s3Key, jsonStr, "application/json");
        } catch (JsonProcessingException e) {
            logger.error("Failed to convert MetaData to JSON: {}", e.getMessage());
            return "Fail to Convert MetaData to JSON; persistenceId " + persistenceId + " Error Message: " + e.getMessage();
        }
    }

    /**
     * Common method to handle the upload of a file to S3.
     *
     * @param s3Key       The key under which the file should be stored in the bucket.
     * @param content     The content to be uploaded.
     * @param contentType The content type of the file.
     * @return A success or failure message indicating the result of the upload.
     */
    private String uploadToS3(String s3Key, String content, String contentType) {
        try (InputStream inputStream = new ByteArrayInputStream(content.getBytes())) {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    .contentLength((long) content.getBytes().length)
                    .build();

            RequestBody requestBody = RequestBody.fromInputStream(inputStream, content.getBytes().length);
            s3Client.putObject(request, requestBody);
            logger.debug("Successfully uploaded to S3: {} / {}", bucketName, s3Key);
            return "Successfully uploaded to S3 " + bucketName + "/" + s3Key;
        } catch (S3Exception e) {
            logger.error("S3Exception: {}", e.getMessage());
            return handleS3Exception(e, s3Key);
        } catch (SdkException | IOException e) {
            logger.error("Exception during S3 upload: {}", e.getMessage());
            return "Fail to upload to S3; Key: " + s3Key + " Error Message: " + e.getMessage();
        }
    }

    /**
     * Handles S3-specific exceptions by logging details and returning an appropriate error message.
     *
     * @param e     The S3 exception.
     * @param s3Key The S3 key associated with the failed operation.
     * @return A descriptive error message.
     */
    private String handleS3Exception(S3Exception e, String s3Key) {
        logger.error("Error Message: {}", e.getMessage());
        logger.error("HTTP Status Code: {}", e.statusCode());
        logger.error("AWS Error Code: {}", e.awsErrorDetails().errorCode());
        logger.error("Request ID: {}", e.requestId());
        return "Fail to upload to S3; Key: " + s3Key + " Error Message: " + e.getMessage();
    }

    /**
     * Generates the S3 key for storing files in the bucket.
     *
     * @param persistenceId The unique ID to include in the key.
     * @param fileName      The name of the file to store.
     * @return The generated S3 key.
     */
    private String generateS3Key(String fileType, String fileName) {
        String s3Folder = getS3Folder();
        return fileType+"/" + s3Folder + "/" + fileName;
    }

    /**
     * Generates the S3 folder path based on the current date.
     *
     * @return The generated S3 folder path in the format "YYYY/MM/DD".
     */
    private String getS3Folder() {
        LocalDate currentDate = LocalDate.now();
        return String.format("%d/%02d/%02d", currentDate.getYear(), currentDate.getMonthValue(), currentDate.getDayOfMonth());
    }
}
