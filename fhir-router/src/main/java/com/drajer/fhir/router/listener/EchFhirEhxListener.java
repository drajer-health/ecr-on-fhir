package com.drajer.fhir.router.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.drajer.fhir.router.service.ProcessMessage;

import io.awspring.cloud.sqs.annotation.SqsListener;
import software.amazon.awssdk.services.sqs.model.Message;

@Service 
public class EchFhirEhxListener {
	@Autowired
	private ProcessMessage processMessage;
	private static final Logger logger = LoggerFactory.getLogger(EchFhirEhxListener.class);	
	
	@SqsListener("${cloud.aws.ehx.queue}")
    public void receiveMessage(Message message) {
		logger.info("SQS EHX Listener Received Message Id : {}"+ message.messageId());
        logger.info("SQS EHX Listener Received Message body : {}"+ message.body());
        logger.info("SQS EHX Listener Received Message attributesAsStrings : {}"+ message.attributesAsStrings());
        		
		processMessage.processListnerMessage(message);
    }
}
