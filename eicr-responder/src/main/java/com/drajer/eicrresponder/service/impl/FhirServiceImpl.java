package com.drajer.eicrresponder.service.impl;

import java.io.File;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.drajer.eicrresponder.model.FhirRequest;
import com.drajer.eicrresponder.model.ResponderRequest;
import com.drajer.eicrresponder.parser.EicrResponderParserContant;
import com.drajer.eicrresponder.service.AmazonClientService;
import com.drajer.eicrresponder.service.Interface.FhirService;
import com.drajer.eicrresponder.util.CommonUtil;
import com.drajer.eicrresponder.util.FhirContextInitializer;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;

/**
 * @author Girish Rao
 *
 */
@Service
@Transactional
public class FhirServiceImpl implements FhirService {
	public static final String RR_DOC_CONTENT_TYPE = "application/xml;charset=utf-8";

	private static final Logger logger = LoggerFactory.getLogger(FhirServiceImpl.class);

	protected FhirContext r4Context = FhirContext.forR4();
	@Autowired
	PrivateKeyGenerator privateKeyGenerator;

	@Autowired
	GenerateAccessToken generateAccessToken;
	
	@Autowired
	AmazonClientService amazonClientService;

	@Autowired
	FhirContextInitializer fhirContextInitializer;

	/**
	 * Submit to Fhir return FhirResponse
	 */
	@Override
	public ResponseEntity<String> submitToFhir(FhirRequest fhirResquest, ResponderRequest responderRequest) {
		String message = "Send bundle to FHIR.";
		logger.info("submit To Fhir:::::" + message);
		try {
			// Initialize the FHIR Context based on FHIR Version
			FhirContext context = fhirContextInitializer.getFhirContext(fhirResquest.getFhirVersion());

			// create reporting bundle
			IParser target = r4Context.newJsonParser(); // new JSON parser
			Bundle rrBundle = target.parseResource(Bundle.class, (String) responderRequest.getRrObject());

			Bundle reportingBundle = (Bundle) CommonUtil.getBundle(rrBundle, responderRequest.getMetadata());

			logger.info("fhirResquest.getFhirServerURL():::::::" + fhirResquest.getFhirServerURL());
			
			//Generate signed private key
			//get private key
			File privateKeyFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX +"private_key.pem");
			String signedJwtToken = privateKeyGenerator.createJwtSignedHMAC(privateKeyFile);
			String accessToken = null;
			if (StringUtils.isNotBlank(signedJwtToken)) {
				// get access token
				JSONObject tokenResponse = generateAccessToken.getAccessToken(signedJwtToken);
				accessToken = tokenResponse.getString(EicrResponderParserContant.ACCESS_TOKEN);
				logger.info("Genertated AccessToken PHA::::"+StringUtils.isNotBlank(accessToken));
			}
			
			
			// Initialize the Client
			IGenericClient client = fhirContextInitializer.createClient(context, fhirResquest.getFhirServerURL(),
					accessToken);
			logger.info("Client after Initializing ::::"+client);

			Bundle responseBundle = fhirContextInitializer.submitProcessMessage(client, reportingBundle);
			logger.info("Fhir response after submit processMessage ::::" + responseBundle.toString());
			message = "Successfully sent bundle to FHIR. ";
		} catch (Exception e) {
			if (e.getMessage().length() > 200) {
				logger.error("Error submiting data to fhir  :::::" + e.getMessage().substring(0, 200));
			} else {
				logger.error("Error submiting data to fhir  :::::" + e.getMessage());
			}
			return (ResponseEntity.status(HttpStatus.OK).body(e.getMessage()));
		}
		return ResponseEntity.status(HttpStatus.OK).body(message);
	}
}
