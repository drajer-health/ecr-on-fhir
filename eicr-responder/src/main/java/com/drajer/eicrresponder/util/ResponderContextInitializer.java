package com.drajer.eicrresponder.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.drajer.eicrresponder.entity.ResponderDataLog;
import com.drajer.eicrresponder.model.FhirRequest;
import com.drajer.eicrresponder.model.Jurisdiction;
import com.drajer.eicrresponder.model.MetaData;
import com.drajer.eicrresponder.model.ResponderRequest;
import com.drajer.eicrresponder.parser.EicrResponderParserContant;
import com.drajer.eicrresponder.service.Interface.FhirService;
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
	private DocumentBuilderFactory factory;
	private DocumentBuilder builder;
	private final String SAVE_RESPONDER_DATA_LOG = "responderlog";
	private static final String RESPONDER_ENDPOINT = "responder.endpoint";
	protected FhirContext r4Context = FhirContext.forR4();
	
	@Autowired
	ResponderDataLogUtil responderDataLogUtil;

	@Autowired
	FhirService fhirService;

	@Autowired
	ProcessJurisdictions processJurs;

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
	public List<ResponseEntity<String>> submitProcessMessage(ResponderRequest responderRequest) {
		logger.info("ResponderRequestContextInitializer submitProcessMessage.......");

	    IParser  target   = r4Context.newJsonParser();      // new JSON parser
	    Bundle eicrBundle = target.parseResource(Bundle.class, (String)responderRequest.getEicrObject());
	    Bundle rrBundle = target.parseResource(Bundle.class, (String)responderRequest.getRrObject());


	    Bundle reportingBundle = (Bundle) CommonUtil.getBundle(eicrBundle,rrBundle,responderRequest.getMetadata(),"pha");
		logger.info("reportingBundle::"+reportingBundle.toString());
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		headers.set("Content-Type", "application/json");
		headers.set("Authorization", "Bearer "+"accessToken");
		List<ResponseEntity<String>> responses = new ArrayList<ResponseEntity<String>>();
		
		//send PHA request for each end point
		responderRequest.getPhaJurisdiction().stream().forEach(jurisdiction -> {
			try {
				logger.info("PHA end point URL from metadata .....::::: {}", jurisdiction.getPhaEndpointUrl());
				String request = r4Context.newJsonParser().encodeResourceToString(reportingBundle);				
				
				HttpEntity<?> entity  = new HttpEntity<>(request, headers);
				ResponseEntity<String> phaResponse = restTemplate.postForEntity(jurisdiction.getPhaEndpointUrl(), entity, String.class);
				
				logger.info("PHA response 111::::", phaResponse);
				responses.add(phaResponse);
			} catch (Exception e) {
				e.printStackTrace();
				if (e.getMessage().length() > 200) {
					logger.error("Error sending pha: "+e.getMessage().substring(0,200));			
				}else {
					logger.error("Error sending pha: "+e.getMessage());			
				}				
				logger.error("Error in sending pha information for URL::::: {}" + jurisdiction.getPhaEndpointUrl());
				responses.add(new ResponseEntity<String>("Failed to send Pha information ", HttpStatus.BAD_REQUEST));
			}
		});
		return responses;
	}

	
	/**
	 * Send files to PHA and FHIR
	 * @param files
	 * @return ResponseEntity<String>
	 */
	public ResponseEntity<String> sendToPha(MultipartFile[] files) {
		String message = "Sent file to PHA";
		try {
			logger.info("Sending files to PHA FHIR 1111.....");
			
			// create ResponderRequest object
			ResponderRequest responderRequest = new ResponderRequest();
			
			// validate all files present
			message = checkFilesValid(files);
			if (!message.isEmpty())
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
			
			// read jurisdiction from metadata.xml
			List<Jurisdiction> jurisdictions = processJurisdictions(files);
												
			// add jurisdiction to responder request object
			responderRequest.setPhaJurisdiction(jurisdictions);

			// add jurisdiction values to metadata object
			MultiValueMap<String, Object> bodyMap = processMetaData(files, jurisdictions);
			logger.info("META_DATA_FILE after adding Jurisdiction 2222::::"
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

			if (jurisdictions.size() > 0) {
				// create bundles for pha and fhir from XML
				createBundle(files, responderRequest);

				// send request to pha
				List<ResponseEntity<String>> resonseEntityPha = new ArrayList<ResponseEntity<String>>();
				StringBuilder processMsg = new StringBuilder();

				logger.info("commonUtil.sendToPha()::::" + CommonUtil.sendToPha());
				if (CommonUtil.sendToPha()) {
					logger.info("jurisdictions.size()::::" + jurisdictions.size());
					if (jurisdictions.size() < 1) {
						return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("No Valid PHA end point found.");
					}
					resonseEntityPha = submitProcessMessage(responderRequest);
					logger.info("resonseEntityPha 3333 ::::" + resonseEntityPha.toString());
					resonseEntityPha.stream().forEach((resonseEntity -> {
						if (resonseEntity.getStatusCode() != HttpStatus.OK)
							processMsg.append(resonseEntity.getBody()).append(System.getProperty("line.separator"));
					}));
				}
				logger.info("processMsg value sendToPha 4444::::" + processMsg);

				// send request to fhir
				FhirRequestConverter fhirRequestConverter = new FhirRequestConverter();
				FhirRequest fhirRequest = fhirRequestConverter
						.convertToFhirRequest(bodyMap.get(EicrResponderParserContant.META_DATA_FILE));
				logger.info("fhirService object:::::"+fhirService);
				ResponseEntity<String> resonseEntityFhir = fhirService.submitToFhir(fhirRequest, responderRequest);
				logger.info("resonseEntityFhir toString::::" + resonseEntityFhir.toString());
				logger.info("resonseEntityFhir getStatusCode::::" + resonseEntityFhir.getStatusCode());

				if (resonseEntityFhir.getStatusCode() != HttpStatus.OK) {
					processMsg.append(resonseEntityFhir.getBody()).append(System.getProperty("line.separator"));
				}
				logger.info("processMsg value ::::" + processMsg);
				if (org.apache.commons.lang3.StringUtils.isNotBlank(processMsg)) {
					return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(processMsg.toString());
				}				
			}else {
				message = "No Jurisdiction found.";
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.OK).body(message);
		}
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
					logger.info("error processing meta data...."  );// + e.getMessage());
//					e.printStackTrace();
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
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = null;
			Document doc = null;
			logger.info("file.getOriginalFilename()::::" + file.getOriginalFilename());
			if (file.getOriginalFilename().equalsIgnoreCase(EicrResponderParserContant.RR_XML)) {
				try {
					dBuilder = dbFactory.newDocumentBuilder();
					doc = dBuilder.parse(file.getInputStream());
					doc.getDocumentElement().normalize();

					factory = DocumentBuilderFactory.newInstance();
					builder = factory.newDocumentBuilder();

					InputStream inputStream = file.getInputStream();
					byte[] bdata = FileCopyUtils.copyToByteArray(inputStream);
					String rrXml = new String(bdata, StandardCharsets.UTF_8);
					doc = builder
							.parse(new BOMInputStream(IOUtils.toInputStream(rrXml, StandardCharsets.UTF_8.name())));
					doc.getDocumentElement().normalize();
					logger.info(
							"doc.getDocumentElement().getNodeName() :::::::" + doc.getDocumentElement().getNodeName());
					// Extract parent for Temple id 2.16.840.1.113883.10.20.15.2.4.2
					Element nd = (Element) EicrResponderParserContant.EICR_PHA_TEMPLATE_EXP.evaluate(doc,
							XPathConstants.NODE);
					if (nd !=null) {
						String nodeName = nd.getNodeName();
						NodeList childeNodes = nd.getChildNodes();
						logger.info("childeNodes length for ::::" + nodeName + " ::::: " + childeNodes.getLength());
						childeNodes = processJurs.getNodeList(childeNodes, EicrResponderParserContant.JUD_PARTICIPANT_ROLE);
						childeNodes = processJurs.getNodeList(childeNodes, EicrResponderParserContant.JUD_ADDR);
						Jurisdiction jurisdictionret = processJurs.getJurisdiction(childeNodes,
								EicrResponderParserContant.JUD_STATE);
						jurisdictionret.setPhaCode(jurisdictionret.getPhaCode());
						jurisdictionret.setPhaEndpointUrl(jurisdictionret.getPhaEndpointUrl());
						logger.info("juridiction end point url:::::" + jurisdictionret.getPhaEndpointUrl());
						jurisdictions.add(jurisdictionret);						
					}else {
						logger.info("Unable to findJurisdiction in xml file");
					}
				} catch (Exception e) {
					//e.printStackTrace();
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
			responderDataLog.setEicrId(Long.parseLong(metaData.getMessageId()));
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
					responderRequest.setRrObject(convertXmlToJsonFhir(content));
					logger.info("After create bundle for 1111 " + EicrResponderParserContant.RR_XML);
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
					responderRequest.setEicrObject(convertXmlToJsonFhir(content));
					logger.info("After create bundle for 2222 " + EicrResponderParserContant.EICR_FHIR_XML);
				} catch (Exception e) {
					logger.error("Error while create bundle for Eicr Fhir " );//+ e.getMessage());
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
			CommonUtil.saveFile(CommonUtil.getTempFilePath()+CommonUtil.getUUID()+".json", output);
		} catch (Exception e) {
			logger.error("Error while create bundle  getBundle" + e.getMessage());
			e.printStackTrace();
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
			inValidFiles.append("RR_XML does not exit.").append(System.getProperty("line.separator"));
		}
		if (!validFiles.contains(EicrResponderParserContant.EICR_FHIR_XML)) {
			inValidFiles.append("EICR_FHIR_XML does not exit.").append(System.getProperty("line.separator"));
		}
		if (!validFiles.contains(EicrResponderParserContant.META_DATA_JSON)) {
			inValidFiles.append("Meta Data Json does not exit.").append(System.getProperty("line.separator"));
		}		
		return inValidFiles.toString();
	}	
}
