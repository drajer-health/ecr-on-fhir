package com.drajer.eicrresponder.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.drajer.eicrresponder.entity.ResponderDataLog;
import com.drajer.eicrresponder.model.FhirRequest;
import com.drajer.eicrresponder.model.Jurisdiction;
import com.drajer.eicrresponder.model.MetaData;
import com.drajer.eicrresponder.model.ResponderRequest;
import com.drajer.eicrresponder.parser.EicrResponderParserContant;
import com.drajer.eicrresponder.service.Interface.FhirService;
import com.drajer.eicrresponder.service.Interface.PostS3Service;
import com.drajer.eicrresponder.service.impl.GenerateAccessToken;
import com.drajer.eicrresponder.service.impl.PrivateKeyGenerator;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

/**
 * @author Girish Rao
 *
 */
@Component
public class ResponderContextInitializer {

	private static final Logger logger = LoggerFactory.getLogger(ResponderContextInitializer.class);
	private final RestTemplate restTemplate;
	private final String SAVE_RESPONDER_DATA_LOG = "responderlog";
	private static final String RESPONDER_ENDPOINT = "responder.endpoint";
	private static final String SUCCESS_MESSAGE ="Send Message to PHA successfull";
	protected FhirContext r4Context = FhirContext.forR4();
	
	@Autowired
	PrivateKeyGenerator privateKeyGenerator;

	@Autowired
	GenerateAccessToken generateAccessToken;
	
	@Autowired
	ResponderDataLogUtil responderDataLogUtil;

	@Autowired
	FhirService fhirService;

	@Autowired
	ProcessJurisdictions processJurs;
	
	@Autowired
	PostS3Service postS3Service;

	/**
	 * @param restTemplateBuilder
	 */
	public ResponderContextInitializer(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}

	/**
	 * Submit PHA data
	 * @param responderRequest
	 * @return ResponseEntity<String> 
	 * 
	 */
	public List<ResponseEntity<String>> submitProcessMessage(ResponderRequest responderRequest,String folderName) {
		logger.info("ResponderRequestContextInitializer submitProcessMessage.......");
		List<ResponseEntity<String>> responses = new ArrayList<ResponseEntity<String>>();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		headers.set("Content-Type", "application/json");			

		IParser  target   = r4Context.newJsonParser();      // new JSON parser
	    Bundle eicrBundle = target.parseResource(Bundle.class, (String)responderRequest.getEicrObject());
	    Bundle rrBundle = target.parseResource(Bundle.class, (String)responderRequest.getRrObject());
	    Bundle reportingBundle = (Bundle) CommonUtil.getBundle(eicrBundle,rrBundle,responderRequest.getMetadata());

		//send PHA request for each end point
		responderRequest.getPhaJurisdiction().stream().forEach(jurisdiction -> {
			logger.info("reportingBundle::"+reportingBundle.toString());			
			try {
				logger.info("PHA end point URL from metadata .....::::: {}", jurisdiction.getPhaEndpointUrl());
				String request = r4Context.newJsonParser().encodeResourceToString(reportingBundle);
				ResponseEntity<String> phaResponse = new ResponseEntity<String>("Sent Pha information for State Code : "+jurisdiction.getPhaCode(), HttpStatus.OK);
				if (jurisdiction.getPhaCode().equalsIgnoreCase("NY")) {
					String s3PhaPostResponse = postS3Service.postToPhaS3(responderRequest,reportingBundle,folderName);
					logger.info("s3PhaPostResponse :::"+s3PhaPostResponse);
					if (StringUtils.isNotBlank(s3PhaPostResponse) && s3PhaPostResponse.contains("Error")){
						phaResponse = new ResponseEntity<String>("Failed to send Pha information for State Code : "+jurisdiction.getPhaCode(), HttpStatus.BAD_REQUEST);
					}					
				}else {
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
					headers.set("Authorization", "Bearer "+accessToken);
				
					HttpEntity<?> entity  = new HttpEntity<>(request, headers);
					phaResponse = restTemplate.postForEntity(jurisdiction.getPhaEndpointUrl(), entity, String.class);					
				}
				logger.info("PHA response submitProcessMessage::::", phaResponse);
				responses.add(phaResponse);
			} catch (Exception e) {
				if (e.getMessage().length() > 200) {
					logger.error("Error sending pha: "+e.getMessage().substring(0,200));			
				}else {
					logger.error("Error sending pha: "+e.getMessage());			
				}				
				logger.error("Error in sending pha information for URL::::: {}" + jurisdiction.getPhaEndpointUrl());
				responses.add(new ResponseEntity<String>("Failed to send Pha information "+jurisdiction.getPhaEndpointUrl(), HttpStatus.BAD_REQUEST));
			}
		});
		return responses;
	}

	
	/**
	 * Send files to PHA and FHIR
	 * @param files
	 * @return ResponseEntity<String>
	 */
	public ResponseEntity<String> sendToPha(MultipartFile[] files,String folderName) {
		String message = "Sent file to PHA";
		try {
			logger.info("Sending files to PHA FHIR .....");
			
			// create ResponderRequest object
			ResponderRequest responderRequest = new ResponderRequest();
			
			// validate all files present
			message = checkFilesValid(files);
			if (!message.isEmpty())
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
			message = SUCCESS_MESSAGE;
			
			// read jurisdiction from metadata.xml
			//commented out below code to send always to NY
//			List<Jurisdiction> jurisdictions = processJurisdictions(files);
			
			
			//create jurisdiction with NY for now should be removed in the future
			//---start
			List<Jurisdiction> jurisdictions = new ArrayList<Jurisdiction>();
			Jurisdiction jurisdiction = new Jurisdiction();
			jurisdiction.setPhaCode("NY");
			jurisdictions.add(jurisdiction );
			//---end
			
			// add jurisdiction to responder request object
			responderRequest.setPhaJurisdiction(jurisdictions);
						
			// add jurisdiction values to metadata object
			MultiValueMap<String, Object> bodyMap = processMetaData(files, jurisdictions);
			logger.info("META_DATA_FILE after adding Jurisdiction ::::"
					+ bodyMap.get(EicrResponderParserContant.META_DATA_FILE));

			if (bodyMap.get(EicrResponderParserContant.META_DATA_FILE) == null) {
				return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
						.body("Error while processing. No Pha data found......");
			}
			// add metadata to responderRequest
			responderRequest.setMetadata((MetaData) bodyMap.get(EicrResponderParserContant.META_DATA_FILE).get(0));
			// log information
			logger.info("EicrResponderParserContant.META_DATA_FILE::::"+bodyMap.get(EicrResponderParserContant.META_DATA_FILE));
			saveDataLog(bodyMap.get(EicrResponderParserContant.META_DATA_FILE));
			
			// create bundles for pha and fhir from XML
			createBundle(files, responderRequest);

			// send request to pha
			List<ResponseEntity<String>> resonseEntityPha = new ArrayList<ResponseEntity<String>>();
			StringBuilder processMsg = new StringBuilder();

			if (jurisdictions.size() < 1) {
				logger.error("No Jurisdictions found.");
			}
			logger.info("commonUtil.sendToPha()::::" + CommonUtil.sendToPha());
			if (CommonUtil.sendToPha() && jurisdictions.size() > 0) {
				logger.info("jurisdictions.size()::::" + jurisdictions.size());
				resonseEntityPha = submitProcessMessage(responderRequest,folderName);
				resonseEntityPha.stream().forEach((resonseEntity -> {
					if (resonseEntity.getStatusCode() != HttpStatus.OK)
						processMsg.append(resonseEntity.getBody()).append(System.getProperty("line.separator"));
				}));
			}

			// send request to fhir
			FhirRequestConverter fhirRequestConverter = new FhirRequestConverter();
			FhirRequest fhirRequest = fhirRequestConverter
					.convertToFhirRequest(bodyMap.get(EicrResponderParserContant.META_DATA_FILE));
			logger.info("fhirService object:::::"+fhirService);
			ResponseEntity<String> resonseEntityFhir = fhirService.submitToFhir(fhirRequest, responderRequest);
			logger.info("resonseEntityFhir getStatusCode::::" + resonseEntityFhir.getStatusCode());

			if (resonseEntityFhir.getStatusCode() != HttpStatus.OK) {
				processMsg.append(resonseEntityFhir.getBody()).append(System.getProperty("line.separator"));
			}
			if (org.apache.commons.lang3.StringUtils.isNotBlank(processMsg)) {
				logger.error(HttpStatus.EXPECTATION_FAILED+processMsg.toString());
			}
			
			String[] s3PostResponse = postS3Service.postToS3(responderRequest, folderName);
			if (Arrays.asList(s3PostResponse).toString().contains("Error")){
				message="Error uploading files to postToS3.";
			}
		} catch (Exception e) {
			if (e.getMessage().length() > 200) {
				logger.error("Error submiting data to sendToPha  :::::" + e.getMessage().substring(0, 200));
			} else {
				logger.error("Error submiting data to sendToPha  :::::" + e.getMessage());
			}
			return ResponseEntity.status(HttpStatus.OK).body(message);
		}
		logger.info("sendToPha message "+message);
		return ResponseEntity.status(HttpStatus.OK).body(message);
	}

	/**
	 * @param file
	 * @return MultiValueMap<String, Object> update metadata with Jurisdiction Read
	 *         and update meta data json file with Jurisdiction Process Meta data
	 */
	public MultiValueMap<String, Object> processMetaData(MultipartFile[] files, List<Jurisdiction> jurisdictions)
			throws StreamReadException, DatabindException, IOException {
		MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
		Arrays.asList(files).stream().forEach(file -> {
			MetaData metaData = null;
			if (file.getOriginalFilename().equalsIgnoreCase(EicrResponderParserContant.META_DATA_JSON)) {
				try {
					// create ObjectMapper instance
					ObjectMapper objectMapper = new ObjectMapper();
					// read json file and convert to MetaData object
					metaData = objectMapper.readValue(file.getInputStream(), MetaData.class);
					metaData.setJurisdictions(jurisdictions);
					// print metaData details
					logger.info("after adding jurisdiction metaDataObj json...." + metaData.toString());
				} catch (Exception e) {
					logger.error("error processing meta data...."  );// + e.getMessage());
				}
				bodyMap.add(EicrResponderParserContant.META_DATA_FILE, metaData);
			}
		});
		return bodyMap;
	}

	/**
	 * @param file
	 * @return Jurisdiction Process jurisdiction info from RR_FHIR.XML
	 * 
	 */
	public List<Jurisdiction> processJurisdictions(MultipartFile[] files)
			throws StreamReadException, DatabindException, IOException {
		logger.info("Getting Jurisdiction from RR_FHIR.XML::::");
		List<Jurisdiction> jurisdictions = new ArrayList<Jurisdiction>();
		Arrays.asList(files).stream().forEach(file -> {
			logger.info("file.getOriginalFilename()::::" + file.getOriginalFilename());
			if (file.getOriginalFilename().equalsIgnoreCase(EicrResponderParserContant.RR_XML)) {
				try {
					InputStream inputStream = file.getInputStream();
					byte[] bdata = FileCopyUtils.copyToByteArray(inputStream);
					String rrXml = new String(bdata, StandardCharsets.UTF_8);

					IParser target = r4Context.newXmlParser();
					Bundle eicrBundle = target.parseResource(Bundle.class, rrXml);

					HashSet<String> jurdStates = new HashSet<String>();

					for (int i = 0; i < eicrBundle.getEntry().size(); i++) {
						boolean foundOrgJud = false;
						Resource bundleResource = eicrBundle.getEntry().get(i).getResource();
						ResourceType resourceType = bundleResource.getResourceType();
						if (resourceType.getPath().equalsIgnoreCase(EicrResponderParserContant.JURD_ORGANIZATION)) {
							org.hl7.fhir.r4.model.Organization org = (org.hl7.fhir.r4.model.Organization) bundleResource;
							List<CodeableConcept> codeableConceptList = org.getType();
							for (CodeableConcept cc : codeableConceptList) {
								List<Coding> codeings = cc.getCoding();
								for (Coding coding : codeings) {
									if (coding.getSystem().contains(EicrResponderParserContant.JURD_SYSTEM_CODE)
											&& coding.getCode().equalsIgnoreCase(EicrResponderParserContant.JURD_CODE_RR7)){
										foundOrgJud = true;
									}
								}
							}
							if (foundOrgJud) {
								List<Address> addresssList = org.getAddress();
								for (Address addr : addresssList) {
									logger.info("addr :::::" + addr.getState());
									jurdStates.add(addr.getState());
								}
							}
						}
					}
					logger.info("juricidations ::::::" + jurdStates.size());
					for (String stateCode : jurdStates) {
						Jurisdiction jurisdictionret = processJurs.getJurisdiction(stateCode);
						logger.info("jurisdictionret ::::::" + jurisdictionret);
						jurisdictions.add(jurisdictionret);
					}
				} catch (Exception e) {
					logger.error("Error while finding jurisdiction :::::"+e.getMessage());
				}
			}
		});
		return jurisdictions;
	}

	public void saveDataLog(List<Object> list) {
		String apiEndPointUrl = CommonUtil.getProperty(RESPONDER_ENDPOINT) + SAVE_RESPONDER_DATA_LOG;
		ResponderDataLog responderDataLog = new ResponderDataLog();
		logger.info("responsder log endpoint url::::" + apiEndPointUrl);
		MetaData metaData = new MetaData();
		logger.info("list.size() ::::" + list.size() );
		if (list.size() > 0) {
			metaData = (MetaData) list.get(0);
			responderDataLog.setEicrId(metaData.getMessageId());
			logger.info("saveDataLog metaData getJurisdictions size::::"+metaData.getJurisdictions().size());
			if (metaData.getJurisdictions().size() > 0)
			responderDataLog.setEndpointUrl(metaData.getJurisdictions().get(0).getPhaEndpointUrl());
			responderDataLog.setEicrReceivedDatatime(Timestamp.from(Instant.now()));
			responderDataLog.setProcessedStatus("Processing");
			logger.info("responderDataLogUtil :::::" + responderDataLogUtil);			
		}
		responderDataLogUtil.createDataLog(apiEndPointUrl, responderDataLog);
	}

	/**
	 * @param files, responderRequest
	 * @return Create bundle for RR_FHIR.xml and EICR_FHIR.xml
	 * 
	 */
	public void createBundle(MultipartFile[] files, ResponderRequest responderRequest) {
		logger.info("createBundle::::");
		Arrays.asList(files).stream().forEach(file -> {
			logger.info("file.getOriginalFilename()::::" + file.getOriginalFilename());
			if (file.getOriginalFilename().equalsIgnoreCase(EicrResponderParserContant.RR_XML)) {
				try {
					InputStream inputStream = file.getInputStream();
				    String content = new BufferedReader(
				    	      new InputStreamReader(inputStream, StandardCharsets.UTF_8))
				    	        .lines()
				    	        .collect(Collectors.joining("\n"));
				    responderRequest.setRrFhirXml(content);
					responderRequest.setRrObject(convertXmlToJsonFhir(content));
					logger.info("After create bundle for " + EicrResponderParserContant.RR_XML);
				} catch (Exception e) {
					logger.error("Error while create bundle for RR" );//+ e.getMessage());
				}
			}
			if (file.getOriginalFilename().equalsIgnoreCase(EicrResponderParserContant.EICR_FHIR_XML)) {
				try {
					InputStream inputStream = file.getInputStream();
				    String content = new BufferedReader(
				    	      new InputStreamReader(inputStream, StandardCharsets.UTF_8))
				    	        .lines()
				    	        .collect(Collectors.joining("\n"));
				    responderRequest.setEicrFhirXml(content);
					responderRequest.setEicrObject(convertXmlToJsonFhir(content));
					logger.info("After create bundle for " + EicrResponderParserContant.EICR_FHIR_XML);
				} catch (Exception e) {
					logger.error("Error while create bundle for Eicr Fhir " );//+ e.getMessage());
				}
			}
			if (file.getOriginalFilename().equalsIgnoreCase(EicrResponderParserContant.EICR_CDA_XML)) {
				try {
					InputStream inputStream = file.getInputStream();
				    String content = new BufferedReader(
				    	      new InputStreamReader(inputStream, StandardCharsets.UTF_8))
				    	        .lines()
				    	        .collect(Collectors.joining("\n"));						
					responderRequest.setEicrCdaXml(content);
					logger.info("After saving EICR CDA XML" + EicrResponderParserContant.EICR_CDA_XML);
				} catch (Exception e) {
					logger.error("Error while saving EICR CDA XML " );//+ e.getMessage());
				}
			}
			if (file.getOriginalFilename().equalsIgnoreCase(EicrResponderParserContant.RR_CDA_XML)) {
				try {
					InputStream inputStream = file.getInputStream();
				    String content = new BufferedReader(
				    	      new InputStreamReader(inputStream, StandardCharsets.UTF_8))
				    	        .lines()
				    	        .collect(Collectors.joining("\n"));						
					responderRequest.setRrCdaXml(content);
					logger.info("After saving RR CDA XML" + EicrResponderParserContant.RR_CDA_XML);
				} catch (Exception e) {
					logger.error("Error while saving RR CDA XML " );//+ e.getMessage());
				}
			}				
		});
	}

	/**
	 * converts inputstream to bundleObj
	 * @param InputStream
	 * @return bundle object
	 * 
	 */	
	public Object convertXmlToJsonFhir(String content) {
		String output = null;
		try {
		    IParser       source   = r4Context.newXmlParser();                         // new XML parser
		    IBaseResource resource = source.parseResource( content );                // parse the resource
		    IParser       target   = r4Context.newJsonParser();                        // new JSON parser
		     output= target.setPrettyPrint( true ).encodeResourceToString( resource ); // output JSON		
//			CommonUtil.saveFile(CommonUtil.getTempFilePath()+CommonUtil.getUUID()+".json", output);
		} catch (Exception e) {
			logger.error("Error while create bundle  getBundle" + e.getMessage());
		}
		return output;
	}
	
	/**
	 * @param file
	 * @return Jurisdiction Process jurisdiction info from RR_FHIR.XML
	 * 
	 */
	public String checkFilesValid(MultipartFile[] files)
			throws StreamReadException, DatabindException, IOException {
		logger.info("checkFilesValid::::");
		List<String> validFiles = new ArrayList<String>();
		StringBuilder inValidFiles = new StringBuilder();
		Arrays.asList(files).stream().forEach(file -> {
			if (file.getOriginalFilename().equalsIgnoreCase(EicrResponderParserContant.RR_XML)) {
				validFiles.add(EicrResponderParserContant.RR_XML);
			}
			if (file.getOriginalFilename().equalsIgnoreCase(EicrResponderParserContant.EICR_FHIR_XML)) {
				validFiles.add(EicrResponderParserContant.EICR_FHIR_XML);
			}
			if (file.getOriginalFilename().equalsIgnoreCase(EicrResponderParserContant.META_DATA_JSON)) {
				validFiles.add(EicrResponderParserContant.META_DATA_JSON);
			}			
		});
		if (!validFiles.contains(EicrResponderParserContant.RR_XML)) {
			inValidFiles.append(EicrResponderParserContant.RR_XML).append(" does not exit.").append(System.getProperty("line.separator"));
		}
		if (!validFiles.contains(EicrResponderParserContant.EICR_FHIR_XML)) {
			inValidFiles.append(EicrResponderParserContant.EICR_FHIR_XML).append(" does not exit.").append(System.getProperty("line.separator"));
		}
		if (!validFiles.contains(EicrResponderParserContant.META_DATA_JSON)) {
			inValidFiles.append(EicrResponderParserContant.META_DATA_JSON).append("Meta Data Json does not exit.").append(System.getProperty("line.separator"));
		}		
		return inValidFiles.toString();
	}
}
