package org.sitenv.spring.service;

import org.sitenv.spring.model.MetaData;

public interface AmazonClientService {

	String uploadBundle3bucket(String messageId, String xml);
	String uploadMetaDataS3bucket(String messageId, MetaData metaData);
	
}
