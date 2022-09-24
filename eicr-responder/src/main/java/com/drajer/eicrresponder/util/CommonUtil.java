package com.drajer.eicrresponder.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Duration;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.MessageHeader.MessageDestinationComponent;
import org.hl7.fhir.r4.model.MessageHeader.MessageSourceComponent;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.drajer.eicrresponder.model.BsaTypes;
import com.drajer.eicrresponder.model.BsaTypes.MessageType;
import com.drajer.eicrresponder.model.MetaData;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class CommonUtil {
	private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);
	private static final String POST_TO_PHA = "responder.posttopha";
	private static final String POST_TO_S3 = "responder.storetos3";
	public static String DEFAULT_VERSION = "1";
	public static String CONTENT_BUNDLE_PROFILE = "http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-content-bundle";
	public static String BUNDLE_REL_URL = "Bundle/";
	public static String MESSAGE_HEADER_PROFILE = "http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-messageheader";
	public static String MESSAGE_TYPE = "http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-messageheader-message-types";
	public static String NAMED_EVENT_URL = "http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-triggerdefinition-namedevents";
	public static String tmpdir = System.getProperty("java.io.tmpdir");
	private static final String CHECK_YES = "YES";
	private static String META_PROFILE ="http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-reporting-bundle";

	public static Properties fetchProperties() {
		Properties properties = new Properties();
		try {
			ResourceLoader resourceLoader = new DefaultResourceLoader();
			org.springframework.core.io.Resource resource = resourceLoader
					.getResource("classpath:application.properties");
			InputStream in = resource.getInputStream();
			properties.load(in);
		} catch (IOException e) {
			logger.info("Error in fetch propertis");
			logger.error(e.getMessage());
		}
		return properties;
	}

	public static String getProperty(String propertyName) {
		String propValue = null;
		Properties prop = CommonUtil.fetchProperties();
		propValue = prop.getProperty(propertyName);
		return propValue;
	}

	public static boolean sendToPha() {
		boolean postToPha = false;
		String propValue = null;
		Properties prop = CommonUtil.fetchProperties();
		propValue = prop.getProperty(POST_TO_PHA);
		if (StringUtils.isNotBlank(propValue) && CHECK_YES.equalsIgnoreCase(propValue)) {
			postToPha = true;
		}
		logger.info("postToPha::::" + postToPha);
		return postToPha;
	}

	public static boolean postToS3() {
		boolean postToS3 = false;
		String propValue = null;
		Properties prop = CommonUtil.fetchProperties();
		propValue = prop.getProperty(POST_TO_S3);
		if (StringUtils.isNotBlank(propValue) && CHECK_YES.equalsIgnoreCase(propValue)) {
			postToS3 = true;
		}
		logger.info("postToS3::::" + postToS3);
		return postToS3;
	}

	public OperationOutcome validateResource(Resource resource, String validatorEndpoint, FhirContext r4Context) {
		OperationOutcome outcome = new OperationOutcome();
		try {
			RestTemplate restTemplate = new RestTemplate();
			// headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
			String request = r4Context.newJsonParser().encodeResourceToString(resource);

			ResponseEntity<String> response = restTemplate.postForEntity(validatorEndpoint, request, String.class);
			outcome = (OperationOutcome) r4Context.newJsonParser().parseResource(response.getBody());

		} catch (Exception e) {
			outcome.addIssue().setSeverity(org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR)
					.setDiagnostics("Failed to parse request body as JSON resource. Error was: " + e.getMessage());
		}

		return outcome;
	}

	public static void saveFile(String fileName, Object content) {
		// create ObjectMapper instance
		ObjectMapper objectMapper = new ObjectMapper();

		// write MetaData object to metaData.json file
		try {
			String indented = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(content);
			objectMapper.writeValue(new File(fileName), indented);
		} catch (IOException e) {
			logger.error("Error while saving file::::"+e.getMessage().substring(0,200));
			e.printStackTrace();
		}
	}

	public static void saveFile(String fileName, String content) {
	    try {
		    Path path = Paths.get(getTempFilePath()+fileName);
		    byte[] strToBytes = content.getBytes();	
			Files.write(path, strToBytes);
		} catch (IOException e) {
			logger.error("Error while saving file::::"+e.getMessage().substring(0,200));
			e.printStackTrace();
		}
	}


	/**
	 * create reporting bundle
	 * 
	 * @param bundle, metaData
	 * @return ResponseEntity<String>
	 * 
	 */
	public static Resource getBundle(Bundle bundle, MetaData metaData) {
		// Create the bundle
		Bundle reportingBundle = new Bundle();

		try {
			reportingBundle.setId(getUUID());
			reportingBundle.setType(BundleType.MESSAGE);
			reportingBundle.setMeta(ActionUtils.getMeta(DEFAULT_VERSION, META_PROFILE)); // need to change profile

			reportingBundle.setTimestamp(Date.from(Instant.now()));

			// Create the Message Header resource.
			MessageHeader header = createMessageHeader(metaData);

			// Setup Message Header to Bundle Linkage.
			Reference ref = new Reference();
			ref.setReference(BUNDLE_REL_URL + bundle.getId());
			List<Reference> refs = new ArrayList<Reference>();
			refs.add(ref);
			header.setFocus(refs);

			// Add the Message Header Resource
			reportingBundle.addEntry(new BundleEntryComponent().setResource(header));

			// Add the document Bundle.
			reportingBundle.addEntry(new BundleEntryComponent().setResource(bundle));
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error while create bundle::::", e);
		}
		return reportingBundle;
	}
	
	public static String getTempFilePath() {
		String tempFilePath = tmpdir+FileSystems.getDefault().getSeparator();
		logger.info("tempFilePath:::::"+tempFilePath);
		return tempFilePath;
	}

	public static String getUUID() {
		UUID uuid = UUID.randomUUID();
		String randomUUID = uuid.toString();
		return randomUUID;
	}

	public static MessageHeader createMessageHeader(MetaData metaData) {

		MessageHeader header = new MessageHeader();
		try {

			header.setId(UUID.randomUUID().toString());
			header.setMeta(ActionUtils.getMeta(DEFAULT_VERSION, MESSAGE_HEADER_PROFILE));

			// Set message type.
			Coding c = new Coding();
			c.setSystem(MESSAGE_TYPE);
			c.setCode(BsaTypes.getMessageTypeString(MessageType.CancerReportMessage));
			header.setEvent(c);

			// set destination
			List<MessageDestinationComponent> mdcs = new ArrayList<MessageDestinationComponent>();

			MessageDestinationComponent mdc = new MessageDestinationComponent();
			mdc.setEndpoint(metaData.getSenderUrl());
			mdcs.add(mdc);
			header.setDestination(mdcs);

			// Set source.
			MessageSourceComponent msc = new MessageSourceComponent();
			msc.setEndpoint(metaData.getSenderUrl());
			header.setSource(msc);

			// Set Reason.
			CodeableConcept cd = new CodeableConcept();
			Coding coding = new Coding();
			coding.setSystem(NAMED_EVENT_URL);
//		    coding.setCode(kd.getNotificationContext().getTriggerEvent());
			cd.addCoding(coding);
			header.setReason(cd);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error while createMessageHeader::::", e);
		}
		return header;
	}
	
	 public static Instant convertDurationToInstant(Duration d) {

		    Instant t = null;
		    String unit = "";

		    if (d != null) {

		      if (d.getCode() != null) {
		        unit = d.getCode();
		      } else if (d.getUnit() != null) {
		        unit = d.getUnit();
		      }

		      if (unit.equalsIgnoreCase("a")) {

		        Calendar c = Calendar.getInstance();
		        c.add(Calendar.YEAR, d.getValue().intValue());
		        t = c.toInstant();

		      } else if (unit.equalsIgnoreCase("mo")) {

		        Calendar c = Calendar.getInstance();
		        c.add(Calendar.MONTH, d.getValue().intValue());
		        t = c.toInstant();

		      } else if (unit.equalsIgnoreCase("wk")) {

		        Calendar c = Calendar.getInstance();
		        c.add(Calendar.DAY_OF_MONTH, (d.getValue().intValue() * 7));
		        t = c.toInstant();

		      } else if (unit.equalsIgnoreCase("d")) {

		        Calendar c = Calendar.getInstance();
		        c.add(Calendar.DAY_OF_MONTH, d.getValue().intValue());
		        t = c.toInstant();

		      } else if (unit.equalsIgnoreCase("h")) {

		        t = new Date().toInstant().plusSeconds(d.getValue().longValue() * 60 * 60);
		      } else if (unit.equalsIgnoreCase("min")) {

		        t = new Date().toInstant().plusSeconds(d.getValue().longValue() * 60);
		      } else if (unit.equalsIgnoreCase("s")) {

		        t = new Date().toInstant().plusSeconds(d.getValue().longValue());
		      } else if (d.getValue() != null) {

		        t = new Date().toInstant().plusSeconds(d.getValue().longValue());
		      } else {
		        t = null;
		      }
		    }

		    return t;
		  }
	 
	 
		public static void saveBundle(String fileName,Bundle resourceBundle) {
			try {
				FhirContext r4Context = FhirContext.forR4();
				IParser parser = r4Context.newJsonParser().setPrettyPrint(true);
				String output = parser.encodeResourceToString(resourceBundle);	
				saveFile(fileName,output);
			}catch(Exception e) {
				logger.error("Error while saving bundle::::"+e.getMessage().substring(0,200));
			}

		}

		public static Bundle getBundle(Bundle eicrBundle, Bundle rrBundle, MetaData metadata) {
			Bundle reportingBundle = (Bundle) getBundle(eicrBundle, metadata);
			// Add the rr Bundle.
			reportingBundle.addEntry(new BundleEntryComponent().setResource(rrBundle));
			
			return reportingBundle;
		}
}