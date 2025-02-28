package com.drajer.fhir.router.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.drajer.fhir.router.service.ProcessMessage;

import software.amazon.awssdk.services.sqs.model.Message;

@Service
public class EchFhirPhaListener {
	@Autowired
	private ProcessMessage processMessage;
	
	private static final Logger logger = LoggerFactory.getLogger(EchFhirPhaListener.class);	
	
//	@SqsListener("${cloud.aws.pha.queue}")
    public void receiveMessage(Message message) {
		logger.info("SQS PHA Listener Received Message Id : {}"+ message.messageId());
		logger.info("SQS PHA Listener Received Message body : {}"+ message.body());
		logger.info("SQS PHA Listener Received Message attributesAsStrings : {}"+ message.attributesAsStrings());
        processMessage.processListnerMessage(message);
	}
}
