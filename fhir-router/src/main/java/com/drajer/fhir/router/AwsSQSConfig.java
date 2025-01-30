package com.drajer.fhir.router;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
public class AwsSQSConfig {

	  @Value("${spring.cloud.aws.credentials.access-key}")
	  private String accessKey ;
	  
	  @Value("${spring.cloud.aws.credentials.secret-key}")
	  private String secretKey ;

	  @Value("${spring.cloud.aws.region.static}")
	  private String region;

	  
	  @Bean
	  SqsAsyncClient sqsAsyncClient(){
	    return SqsAsyncClient
	      .builder()
	      .region(Region.of(region))
	      .credentialsProvider(StaticCredentialsProvider
	        .create(AwsBasicCredentials.create(accessKey, secretKey)))
	      .build();
	  }

}
