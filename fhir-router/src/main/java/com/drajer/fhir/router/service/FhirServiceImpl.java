package com.drajer.fhir.router.service;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.codesystems.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.drajer.fhir.router.util.FhirContextInitializer;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import jakarta.transaction.Transactional;

/**
 * @author Girish Rao
 *
 */
@Service
@Transactional
public class FhirServiceImpl {
	public static final String RR_DOC_CONTENT_TYPE = "application/xml;charset=utf-8";
	private static final Logger logger = LoggerFactory.getLogger(FhirServiceImpl.class);
	protected FhirContext r4Context = FhirContext.forR4();

	@Autowired
	FhirContextInitializer fhirContextInitializer;

	/**
	 * Submit to Fhir return FhirResponse
	 */
	public ResponseEntity<String> submitToFhir(String fhirUrl, String accessToken, String requestData) {
		String message = "Send bundle to FHIR.";
		logger.info("submit To Fhir : {} ", message);
		try {
			// Initialize the FHIR Context based on FHIR Version
			FhirContext context = fhirContextInitializer.getFhirContext("R4");

			logger.info("requestData before creating bundle : {} ");
			// create reporting bundle
//			IParser target = r4Context.newJsonParser(); // new JSON parser
//			Bundle bundle = target.parseResource(Bundle.class, requestData);

			
			
			IParser       source   = r4Context.newXmlParser();                         // new XML parser
			IBaseResource resource = source.parseResource( requestData );                // parse the resource
			IParser       target   = context.newJsonParser();                        // new JSON parser
			String jsonResource =  target.setPrettyPrint( true ).encodeResourceToString( resource ); 
			logger.info("jsonResource string   : {} " , jsonResource);
			Bundle bundle = target.parseResource(Bundle.class, jsonResource);
			
//			 target.setPrettyPrint( true ).encodeResourceToString( resource ); // output JSON
//			 Bundle bundle = target.parseResource(Bundle.class, requestData);
			 
			logger.info("FHIR URL : {} ",fhirUrl);
			// Initialize the Client
			IGenericClient client = fhirContextInitializer.createClient(context, fhirUrl, accessToken);
			logger.info("Client after Initializing : {} ", client);

			Bundle responseBundle = fhirContextInitializer.submitProcessMessage(client, bundle);
			logger.info("Fhir response after submit processMessage : {} ", responseBundle.toString());
			message = target.encodeResourceToString(responseBundle);

			logger.info("Successfully sent bundle to FHIR. ");
		} catch (Exception e) {
			if (e.getMessage().length() > 200) {
				logger.error("Error submiting data to fhir  : {} ", e.getMessage().substring(0, 200));
			} else {
				logger.error("Error submiting data to fhir  : {} ", e.getMessage());
			}
			return (ResponseEntity.status(HttpStatus.OK).body(e.getMessage()));
		}
		return ResponseEntity.status(HttpStatus.OK).body(message);
	}
	
}
