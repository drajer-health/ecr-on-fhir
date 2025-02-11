package com.drajer.fhir.router.service;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@Service
public class SecretManagerDetails {
	private static final Logger logger = LoggerFactory.getLogger(SecretManagerDetails.class);

	public JSONObject getSecret(String secretName, String awsRegion, String accessKey, String secretKey) {
		JSONObject secretValues;

		Region region = Region.of(awsRegion);

		logger.info("Secret Name :" + secretName);

		// Create a Secrets Manager client
		SecretsManagerClient client = SecretsManagerClient.builder().region(region)
				.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
				.build();

		GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder().secretId(secretName).build();

		GetSecretValueResponse getSecretValueResponse;

		try {
			getSecretValueResponse = client.getSecretValue(getSecretValueRequest);

			logger.info("GetSecretValueResponse string  ::::" + getSecretValueResponse.toString());

			String secret = getSecretValueResponse.secretString();

			logger.info(" secret string ::::" + secret);

			JSONObject jsonObject = new JSONObject(secret);
			String jsonKey = jsonObject.keys().next();

			logger.info("jsonObject string ::::" + jsonObject.toString());
			logger.info("jsonKey ::::" + jsonKey);
			secretValues = new JSONObject(jsonObject.getString(jsonKey));
			logger.info(" secretValues ::::" + secretValues);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
			throw e;
		}
		return secretValues;
	}
}
