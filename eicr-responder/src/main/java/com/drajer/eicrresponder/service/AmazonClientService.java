package com.drajer.eicrresponder.service;

public interface AmazonClientService {

	String uploads3bucket(String messageId, String xml);
	String uploadPhaS3bucket(String messageId, String xml);
	
}
