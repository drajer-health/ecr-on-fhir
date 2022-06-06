package com.drajer.eicrresponder.service.impl;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.drajer.eicrresponder.service.Interface.ResponderService;
import com.drajer.eicrresponder.util.ResponderContextInitializer;

/**
 * @author Girish Rao
 *
 */
@Service
@Transactional
public class ResponderServiceImpl implements ResponderService {

	@Autowired
	ResponderContextInitializer responderContextInitializer;

	private static final Logger logger = LoggerFactory.getLogger(ResponderServiceImpl.class);

	/**
	 * return ResponseEntity<String
	 */
	@Override
	public ResponseEntity<String> sendResponder(MultipartFile[] files,String folderName) {
		logger.info("Sending RR Bundle....");
		return responderContextInitializer.sendToPha(files, folderName);
	}
}
