package com.drajer.eicrresponder.controller;

import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.drajer.eicrresponder.service.Interface.ResponderService;
import com.drajer.eicrresponder.service.impl.SubmitReportImpl;

/**
 * @author Girish Rao 
 * Process eicr data send bundle to PHA and 
 * Health care provider
 * 
 */
@RestController
@CrossOrigin
@RequestMapping("/api")
public class ReceiveDataController {

	private static final Logger logger = LoggerFactory.getLogger(ReceiveDataController.class);

	@Autowired
	ResponderService responderService;
	
	@Autowired
	SubmitReportImpl submitReportImpl;	

	// POST method to send to pha
	/**
	 * @param files
	 * @return ResponseEntity<String> Send bundle to health care and PHA
	 */
	@PostMapping("/receiveeicrrdata")
	public ResponseEntity<String> uploadFiles(@RequestParam("files") MultipartFile[] files,String folderName) {
		logger.info("ReceiveDataController folderName:    " +folderName);
		return responderService.sendResponder(files,folderName);
	}
	
	@PostMapping("/postToPha")
	public String sendMessageToPha(Bundle theMessageToProcess, String fhirServerBaseURL) {
		logger.info("Successfully Receieved data for PHA");
		return submitReportImpl.submitFhirOutput(theMessageToProcess, fhirServerBaseURL);
	}

	@PostMapping("/postToHealthCare")
	public Bundle sendMessageToHealthcare(String theMessageToProcess) {
		logger.info("Successfully Receieved data for Health Care.");
		return null;
	}
}
