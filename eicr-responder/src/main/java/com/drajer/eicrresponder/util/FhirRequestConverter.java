package com.drajer.eicrresponder.util;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.drajer.eicrresponder.model.FhirRequest;
import com.drajer.eicrresponder.model.MetaData;

/**
 * @author Girish Rao
 *
 */
@Component
public class FhirRequestConverter {
	private static final Logger logger = LoggerFactory.getLogger(FhirRequestConverter.class);
	private static final String R4 = "R4";

	/**
	 * @param jsonObject
	 * @return
	 */
	public FhirRequest convertToFhirRequest(Object metadataObject) {
		FhirRequest fhirRequestObj = new FhirRequest();
		fhirRequestObj.setFhirVersion(R4);
		if (metadataObject != null) {
			logger.info("metadataObject before set::::" + metadataObject.toString());
			ArrayList<?> metaDataList = (ArrayList<?>) metadataObject;
			MetaData metaDataObj = (MetaData) metaDataList.get(0);

			fhirRequestObj.setFhirServerURL(metaDataObj.getSenderUrl());
			metaDataObj.setJurisdictions(metaDataObj.getJurisdictions());
			logger.info("FhirRequestConverter . convertToFhirRequest ...." + fhirRequestObj.toString());
		}
		return fhirRequestObj;
	}
}
