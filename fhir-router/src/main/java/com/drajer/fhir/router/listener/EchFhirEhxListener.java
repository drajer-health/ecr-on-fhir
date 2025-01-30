package com.drajer.fhir.router.listener;

import org.springframework.stereotype.Service;

import io.awspring.cloud.sqs.annotation.SqsListener;
import software.amazon.awssdk.services.sqs.model.Message;

@Service
public class EchFhirEhxListener {
	
	
//	@SqsListener("${cloud.aws.ehx.queue}")
    public void receiveMessage(Message message) {
        System.out.println("SQS EHS Message Received : {}"+ message);
	}
}
