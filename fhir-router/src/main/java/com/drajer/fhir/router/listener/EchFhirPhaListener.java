package com.drajer.fhir.router.listener;

import java.util.Map;

import org.springframework.stereotype.Service;

import io.awspring.cloud.sqs.annotation.SqsListener;

@Service
public class EchFhirPhaListener {
	
	@SqsListener("${cloud.aws.pha.queue}")
    public void receiveMessage(Map<String, Object> message) {
        System.out.println("SQS PHA Message Received : {}"+ message);
	}
}
