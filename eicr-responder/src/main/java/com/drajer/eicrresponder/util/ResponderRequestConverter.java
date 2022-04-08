package com.drajer.eicrresponder.util;

import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.drajer.eicrresponder.entity.PhaRouting;
import com.drajer.eicrresponder.model.ResponderRequest;
import com.drajer.eicrresponder.service.ClientService;

/**
 * @author Girish Rao
 *
 */
@Component
public class ResponderRequestConverter {
	private static final Logger logger = LoggerFactory.getLogger(ResponderRequestConverter.class);

	@Autowired
	ClientService clientService;

	/**
	 * @param reqObject
	 * @return responderRequestObj
	 */
	public ResponderRequest convertToPhaRequest(Object reqObject) {
		ResponderRequest responderRequestObj = new ResponderRequest();
		logger.info("jsonObject before set::::" + reqObject);
		LinkedHashMap<?, ?> jsonObject = (LinkedHashMap<?, ?>) reqObject;
		logger.info("jsonObject after convertion::::" + jsonObject);
		logger.info("jsonObject organization::::" + jsonObject.get("organization"));

		PhaRouting phaRouting = getPhaURL((String) jsonObject.get("organization"));
		responderRequestObj.setRetryCount(phaRouting.getRetryCount());
		logger.info("responderRequestObj . convertToPhaRequest ...." + responderRequestObj.toString());
		return responderRequestObj;
	}

	/**
	 * @param phaAgencyCode
	 * @return PhaRouting
	 */
	public PhaRouting getPhaURL(String phaAgencyCode) {
		return clientService.getPhaUrl(phaAgencyCode);
	}
}
