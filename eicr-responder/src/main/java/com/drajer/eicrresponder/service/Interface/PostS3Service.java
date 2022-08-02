package com.drajer.eicrresponder.service.Interface;

import org.hl7.fhir.r4.model.Bundle;

import com.drajer.eicrresponder.model.ResponderRequest;

public interface PostS3Service {
	String[] postToS3( ResponderRequest responderRequest,String folderName);
	String postToPhaS3( Bundle reportingBundle,String folderName);
}
