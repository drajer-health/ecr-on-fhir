package com.drajer.fhir.router.service;

import java.security.KeyStoreException;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.drajer.fhir.router.constant.FhirRouterConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.sqs.model.Message;

@Service
public class ProcessMessage {

	@Autowired
	private SecretManagerDetails secretManagerDetails;

	@Autowired
	private GenerateAccessToken generateAccessToken;

	@Autowired
	private FhirServiceImpl fhirServiceImpl;

	@Value("${cloud.aws.region.static}")
	String awsRegion;

	@Value("${spring.cloud.aws.credentials.access-key}")
	String awsAccessKey;

	@Value("${spring.cloud.aws.credentials.secret-key}")
	String awsSecretKey;

	private static final Logger logger = LoggerFactory.getLogger(ProcessMessage.class);

	public void processListnerMessage(Message message, String folderName) {
		String jsonBody = message.body();
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = null;
		try {
			rootNode = objectMapper.readTree(jsonBody);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("objectMapper error  : " + e.getMessage());
		}

		JsonNode detailNode = rootNode.get("detail");
		if (detailNode == null) {
			detailNode = rootNode;
		}
		JsonNode bucketNode = detailNode.get("bucket");
		JsonNode keyObjectNode = detailNode.get("object");

		String bucket = bucketNode.get("name").asText();
		String key = keyObjectNode.get("key").asText(); // record.getS3().getObject().getKey();

		logger.info("BucketName : " + bucket);
		logger.info("Key:" + key);

		AwsBasicCredentials credentials = AwsBasicCredentials.create(awsAccessKey, awsSecretKey);
		S3Client s3client = S3Client.builder().region(Region.of(awsRegion))
				.credentialsProvider(StaticCredentialsProvider.create(credentials)).build();

		GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(key).build();
		ResponseInputStream<GetObjectResponse> responseInputStream = s3client.getObject(getObjectRequest);
		logger.info("ResponseInputStream  to string :" + responseInputStream.response().toString());
		GetObjectResponse objectResponse = responseInputStream.response();
		logger.info("metadata  to string :" + objectResponse.metadata().toString());
		Map<String, String> metadataMap = objectResponse.metadata();
		logger.info("metadataMap  :" + metadataMap);
		logger.info("metadataMap  size:" + metadataMap.size());
		String secretName = metadataMap.get("secretname");
		logger.info("metadataMap  secretname:" + secretName);
		// get secrets
		JSONObject secretValues = secretManagerDetails.getSecret(secretName, awsRegion, awsAccessKey, awsSecretKey);
		logger.info("Secret token url : {}" + secretValues.get(FhirRouterConstants.TOKEN_URL));

		String tokenEndpoint = (String) secretValues.get(FhirRouterConstants.TOKEN_URL);
		String clientId = (String) secretValues.get(FhirRouterConstants.CLIENT_ID);
		String jwtSignToken = (String) secretValues.get(FhirRouterConstants.CERT_PRIVATE_KEY);
		String fhirUrl = (String) secretValues.get(FhirRouterConstants.FHIR_URL);
		String accessToken = null;
		// get token
		try {
			JSONObject accessTokenObj = generateAccessToken.getAccessToken(jwtSignToken, tokenEndpoint, clientId);
			logger.info("accessTokenObj :" + accessTokenObj.toString());
			accessToken = accessTokenObj.toString();
		} catch (KeyStoreException e) {
			logger.info("Error get access token :::: " + e.getMessage());
			e.printStackTrace();
		}

		// read the file
		key = key.replace("FHIROutboundV2", folderName);
		key = key.replace("FHIROutboundPHAV2", folderName);

		logger.info("Read file from  : " , key);
		getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(key).build();
		responseInputStream = s3client.getObject(getObjectRequest);

		ResponseEntity<String> operationOutcome = fhirServiceImpl.submitToFhir(fhirUrl, key, accessToken);
		String responseBody = operationOutcome.getBody();
		if (operationOutcome.getStatusCode().is2xxSuccessful()) {
		    // Process the response body
		    logger.info("Fhir response : {} ", responseBody);
		} else {
		    // Handle error scenarios
			logger.error("Request failed with status code: {} " , operationOutcome.getStatusCode());
		}
		
		// put object to s3
		//Key
		key = key.replace(folderName,"FHIRValidationOutputXML");
		logger.info("Key before store to S3 : {} ", key);
		storeToS3(s3client,bucket,key,responseBody);
	}

	
	public void storeToS3(S3Client s3Client, String bucketName, String objectKey, String strValue) {
		
        PutObjectRequest putOb = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        PutObjectResponse response = s3Client.putObject(putOb,
                RequestBody.fromString(strValue));
        logger.info( "S3 Put object response : ", response.eTag());
	}
}
