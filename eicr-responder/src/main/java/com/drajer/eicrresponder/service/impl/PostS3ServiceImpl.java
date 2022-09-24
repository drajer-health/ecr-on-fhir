package com.drajer.eicrresponder.service.impl;

import java.util.Arrays;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.drajer.eicrresponder.model.MetaData;
import com.drajer.eicrresponder.model.ResponderRequest;
import com.drajer.eicrresponder.parser.EicrResponderParserContant;
import com.drajer.eicrresponder.service.AmazonClientService;
import com.drajer.eicrresponder.service.Interface.PostS3Service;
import com.drajer.eicrresponder.util.CommonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

/**
 * @author Girish Rao
 *
 */
@Service
@Transactional
public class PostS3ServiceImpl implements PostS3Service {

	private static final Logger logger = LoggerFactory.getLogger(PostS3ServiceImpl.class);

	protected FhirContext r4Context = FhirContext.forR4();

	@Autowired
	AmazonClientService amazonClientService;

	@Override
	public String[] postToS3(ResponderRequest responderRequest,String folderName) {
		String[] s3PostResponse = new String[3];
			try {
				// create reporting bundle
				Bundle reportingBundle = getBundle((String) responderRequest.getRrObject(),
						responderRequest.getMetadata());
				String request = r4Context.newJsonParser().encodeResourceToString(reportingBundle);
				// Check for Message Id and Error Handling
				logger.info("before uploads3bucket::::" + amazonClientService);
				s3PostResponse[0] = amazonClientService.uploads3bucket(
						folderName+EicrResponderParserContant.RR_JSON,
					 getOutput(request));
				logger.info("after upload RR_XML response::::" + s3PostResponse[0]);

				reportingBundle = getBundle((String) responderRequest.getEicrObject(), responderRequest.getMetadata());
				request = r4Context.newJsonParser().encodeResourceToString(reportingBundle);
				s3PostResponse[1] = amazonClientService.uploads3bucket(
						folderName+EicrResponderParserContant.EICR_FHIR_JSON,
						getOutput(request));
				logger.info("after upload EICR_FHIR_XML response ::::" + s3PostResponse[1]);

				ObjectMapper mapper = new ObjectMapper();
				String jsonStr = mapper.writeValueAsString(responderRequest.getMetadata());
				logger.info("before metadata josn uploads3bucket ::::"+reportingBundle.fhirType());
				s3PostResponse[2] = amazonClientService.uploads3bucket(
						folderName+EicrResponderParserContant.META_DATA_JSON,
						jsonStr);
				logger.info("after upload META_DATA_JSON response ::::" + s3PostResponse[2]);

			} catch (Exception e) {
				e.printStackTrace();
				logger.info("Error posting to s3 bucket" + e.getMessage());
			}
		logger.info("s3PostResponse postToS3::::"+Arrays.asList(s3PostResponse).toString());
		return s3PostResponse;
	}

	@Override
	public String postToPhaS3(ResponderRequest responderRequest, Bundle reportingBundle,String folderName) {
		StringBuilder s3PhaPostResponse = new StringBuilder();
			try {
				String request = r4Context.newJsonParser().encodeResourceToString(reportingBundle);	
				
//				logger.info("before create file::::");
//				// save to file
//				CommonUtil.saveFile(EicrResponderParserContant.RR_JSON, request);
//				CommonUtil.saveFile(EicrResponderParserContant.RR_CDA_XML, responderRequest.getRrCdaXml());
//				CommonUtil.saveFile(EicrResponderParserContant.EICR_CDA_XML, responderRequest.getEicrCdaXml());
				
				// Check for Message Id and Error Handling
				logger.info("before uploadsPhaS3bucket::::" + amazonClientService);
				String phaResponse = amazonClientService.uploadPhaS3bucket(
						folderName+EicrResponderParserContant.RR_JSON,
					 getOutput(request));
				s3PhaPostResponse.append(phaResponse);
				s3PhaPostResponse.append(System.getProperty("line.separator"));
				logger.info("after upload {} response:::: {}" ,EicrResponderParserContant.RR_JSON, s3PhaPostResponse.toString());
				
				// POST RR_CDA_XML
				if (StringUtils.isNotBlank(responderRequest.getRrCdaXml())) {
					phaResponse = amazonClientService.uploadPhaS3bucket(
							folderName+EicrResponderParserContant.RR_CDA_XML,
							responderRequest.getRrCdaXml());
					s3PhaPostResponse.append(phaResponse);
					s3PhaPostResponse.append(System.getProperty("line.separator"));
					logger.info("after upload {} response:::: {}" ,EicrResponderParserContant.RR_CDA_XML, s3PhaPostResponse.toString());					
				}else {
					logger.info("{} not found or empty." ,EicrResponderParserContant.RR_CDA_XML);	
				}
				logger.info("after upload {} response:::: {}" ,EicrResponderParserContant.RR_CDA_XML, s3PhaPostResponse.toString());
				
				// POST EICR_CDA_XML
				if (StringUtils.isNotBlank(responderRequest.getEicrCdaXml())) {
					phaResponse = amazonClientService.uploadPhaS3bucket(
							folderName+EicrResponderParserContant.EICR_CDA_XML,
							responderRequest.getEicrCdaXml());
					s3PhaPostResponse.append(phaResponse);
					s3PhaPostResponse.append(System.getProperty("line.separator"));				
					logger.info("after upload {} response:::: {}" ,EicrResponderParserContant.EICR_CDA_XML, s3PhaPostResponse.toString());					
				}else {
					logger.info("{} not found or empty." ,EicrResponderParserContant.EICR_CDA_XML);	
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("Error posting to phas3 bucket" + e.getMessage());
			}
		logger.info("s3PhaPostResponse postToPhaS3::::"+s3PhaPostResponse.toString());
		return s3PhaPostResponse.toString();
	}	
	
	private String getOutput(String request) {
		IParser ip = r4Context.newJsonParser();
		IBaseResource ri = ip.parseResource(request);
		String output = ip.setPrettyPrint(true).encodeResourceToString(ri); //
		return output;
	}

	private Bundle getBundle(String bundleObj, MetaData metaData) {
		// create reporting bundle
		IParser target = r4Context.newJsonParser(); // new JSON parser
		Bundle rrBundle = target.parseResource(Bundle.class, bundleObj);
		return (Bundle) CommonUtil.getBundle(rrBundle, metaData);
	}

}
