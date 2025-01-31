package com.drajer.fhir.router.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.drajer.fhir.router.service.SecretManagerDetails;

import io.awspring.cloud.sqs.annotation.SqsListener;
import software.amazon.awssdk.services.sqs.model.Message;

@Service
public class EchFhirEhxListener {

	@Autowired
	private SecretManagerDetails secretManagerDetails;

	@Value("${aws.secret.ehx.name}")
	String secretName;
	
	@Value("${cloud.aws.region.static}")
	String awsRegion;		
	
	@Value("${spring.cloud.aws.credentials.access-key}")
	String awsAccessKey;
	
	@Value("${spring.cloud.aws.credentials.secret-key}")
	String awsSecretKey;	
	
	@SqsListener("${cloud.aws.ehx.queue}")
    public void receiveMessage(Message message) {
        System.out.println("EHX SQS Listener Received Message Id : {}"+ message.messageId());
        System.out.println("EHX SQS Listener Received Message body : {}"+ message.body());
        System.out.println("EHX SQS Listener Received Message attributesAsStrings : {}"+ message.attributesAsStrings());
        //get secrets
        secretManagerDetails.getSecret(secretName, awsRegion, awsAccessKey, awsSecretKey);
        
    
    }
}
