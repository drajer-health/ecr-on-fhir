package com.drajer.eicrresponder.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.transaction.Transactional;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
	private static final String RESPONDER_VALIDATOR = "responder.validator";

	private static final Logger logger = LoggerFactory.getLogger(FhirServiceImpl.class);
	
	protected FhirContext r4Context = FhirContext.forR4();
	AmazonClientService amazonClientService;

	@Autowired
	FhirContextInitializer fhirContextInitializer;

	/**
	 * Submit to Fhir
	 * return FhirResponse
	 */
	@Override
	public ResponseEntity<String> submitToFhir(FhirRequest fhirResquest, ResponderRequest responderRequest) {
		String message = "Send bundle to FHIR.";
		logger.info("submit To Fhir:::::"+message);
		OperationOutcome outcome = new OperationOutcome();
		try {
			// Initialize the FHIR Context based on FHIR Version
			FhirContext context = fhirContextInitializer.getFhirContext(fhirResquest.getFhirVersion());
			String validatorEndpoint = CommonUtil.getProperty(RESPONDER_VALIDATOR);
			logger.info("fhir validator endpoint:::::"+validatorEndpoint);
			
			// create reporting bundle
		    IParser  target   = r4Context.newJsonParser();   // new JSON parser
		    Bundle rrBundle = target.parseResource(Bundle.class, (String)responderRequest.getRrObject());
			
			Bundle reportingBundle = (Bundle) CommonUtil.getBundle(rrBundle,responderRequest.getMetadata(),"rr");
			String request = r4Context.newJsonParser().encodeResourceToString(reportingBundle);
			
			if (CommonUtil.postToS3()) {
				try {
				    // write to s3 
//					logger.info("request after encodeResourceToString:::::"+request);
					// to store in S3 convert to XML
					//Convert JSON to XML
					IParser ip = r4Context.newJsonParser(),
							op = r4Context.newXmlParser();
					IBaseResource ri = ip.parseResource(request);
					logger.info("before encodeResourceToString::::"+ri);
					String output = op.setPrettyPrint(true).encodeResourceToString(ri); //
					//System.out.println("XML Output === "+ "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+output );

					//Check for Message Id and Error Handling 
					amazonClientService.uploads3bucket(UUID.randomUUID().toString()+ "/"+EicrResponderParserContant.RR_XML , "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+output);
				}catch(Exception e) {
					logger.info("Error posting to s3 bucket");
//					e.printStackTrace();
				}
			}

			
				logger.info("ahirResquest.getFhirServerURL():::::::"+fhirResquest.getFhirServerURL());
				// Initialize the Client
				IGenericClient client = fhirContextInitializer.createClient(context, fhirResquest.getFhirServerURL(),
						fhirResquest.getAccessToken());

				Bundle responseBundle = fhirContextInitializer.submitProcessMessage(client, reportingBundle);
				logger.info("Fhir response after submit processMessage ::::" + responseBundle.toString());
		} catch (Exception e) {
//			e.printStackTrace();
			if (e.getMessage().length() > 200) {
				logger.info("Error submiting data to fhir  :::::"+e.getMessage().substring(0,200));				
			}else {
				logger.info("Error submiting data to fhir  :::::"+e.getMessage());				
			}
			return (ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage().substring(0,200)));
		}
		return ResponseEntity.status(HttpStatus.OK).body(message);
	}
}
