package com.drajer.eicrresponder.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.drajer.eicrresponder.entity.ResponderDataLog;

@Component
public class ResponderDataLogUtil {
	private static final Logger logger = LoggerFactory.getLogger(ResponderDataLogUtil.class);
	private final RestTemplate restTemplate;

	public ResponderDataLogUtil(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}

	public ResponseEntity<String> createDataLog(String endPointUrl, Object responderDataLog) {
		logger.info("createDataLog.......");
//		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		ResponseEntity<String> returnResponse = null;
		try {
			logger.info("endPointUrl::::" + endPointUrl);
			logger.info("restTemplate::::" + restTemplate);
			logger.info("responderDataLog::::" + responderDataLog.toString());
			ResponseEntity<ResponderDataLog> responseDataLog = restTemplate
					.postForEntity(endPointUrl, responderDataLog, ResponderDataLog.class);
			logger.info(" ResponseEntity<ResponderDataLog>::::" + responseDataLog);
			returnResponse = new ResponseEntity<String>(endPointUrl, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error in Saving responder log information for URL::::: {}", endPointUrl, e.getMessage());
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		return returnResponse;
	}

}
