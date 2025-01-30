package com.drajer.fhir.router.listener;

import org.springframework.stereotype.Service;

import io.awspring.cloud.sqs.annotation.SqsListener;
import software.amazon.awssdk.services.sqs.model.Message;

@Service
public class EchFhirPhaListener {
	
	@SqsListener("${cloud.aws.pha.queue}")
    public void receiveMessage(Message message) {
        System.out.println("SQS PHA Listener Received Message Id : {}"+ message.messageId());
        System.out.println("SQS PHA Listener Received Message body : {}"+ message.body());
        System.out.println("SQS PHA Listener Received Message attributesAsStrings : {}"+ message.attributesAsStrings());
        
	}
}
