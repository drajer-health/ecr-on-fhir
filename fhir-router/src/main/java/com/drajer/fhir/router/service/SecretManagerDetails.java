package com.drajer.fhir.router.service;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@Service
public class SecretManagerDetails {

	public JSONObject getSecret(String secretName, String awsRegion, String accessKey, String secretKey) {
		JSONObject secretValues;
		
		Region region = Region.of(awsRegion);
		
		// Create a Secrets Manager client
		SecretsManagerClient client = SecretsManagerClient.builder().region(region)
				.credentialsProvider(StaticCredentialsProvider
				        .create(AwsBasicCredentials.create(accessKey, secretKey)))
				.build();

		GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder().secretId(secretName).build();

		GetSecretValueResponse getSecretValueResponse;

		try {
			getSecretValueResponse = client.getSecretValue(getSecretValueRequest);

		String secret = getSecretValueResponse.secretString();
	   
	    JSONObject jsonObject = new JSONObject(secret);   
	    String jsonKey =  jsonObject.keys().next();
	    secretValues = new JSONObject(jsonObject.getString(jsonKey));
	    System.out.println(" secretValues ::::"+ secretValues); 
		} catch (Exception e) {
			System.err.println(e.getMessage());
			throw e;
		}
	    return secretValues;
	}
}
