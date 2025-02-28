package com.drajer.fhir.router.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.MessageHeader.MessageDestinationComponent;
import org.hl7.fhir.r4.model.MessageHeader.MessageSourceComponent;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UriType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
	private static final String DEFAULT_VERSION = "1";
	private static final String MESSAGE_HEADER_PROFILE =
		      "http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-messageheader";
	  private static final String MESSAGE_PROCESSING_CATEGORY_EXT_URL =
		      "http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-message-processing-category-extension";
	  private static final String MESSAGE_PROCESSING_CATEGORY_CODE = "notification";
	  private static final String MESSAGE_TYPE_URL =
		      "http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-message-types-codesystem";	
	  private static final String NAMED_EVENT_URL =
		      "http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-triggerdefinition-namedevents";
	  private static final String BUNDLE_REL_URL = "Bundle/";
	  private static final String RR_PROFILE ="http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-reporting-bundle";
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
			
			r4Context.newJsonParser().encodeResourceToString(resource);
			
//			logger.info("jsonResource string   : {} " , jsonResource);
			Bundle contentBundle = target.parseResource(Bundle.class, jsonResource);
			
//			 target.setPrettyPrint( true ).encodeResourceToString( resource ); // output JSON
//			 Bundle bundle = target.parseResource(Bundle.class, requestData);
			
		      logger.info(" Creating a FHIR Eicr Report ");
		      Bundle reportingBundle = createReportingBundle(RR_PROFILE);
		      MessageHeader mh = createMessageHeader(false, contentBundle,fhirUrl);

		      // Add the Message Header Resource
		      BundleEntryComponent bec = new BundleEntryComponent();
		      bec.setResource(mh);
		      bec.setFullUrl(
		          StringUtils.stripEnd(fhirUrl, "/")
		              + "/"
		              + mh.getResourceType().toString()
		              + "/"
		              + mh.getIdElement().getIdPart());

		      reportingBundle.addEntry(bec);

		      // Add the Content Bundle.
		      reportingBundle.addEntry(new BundleEntryComponent().setResource(contentBundle));			
			
			
			
			logger.info("FHIR URL : {} ",fhirUrl);
			// Initialize the Client
			IGenericClient client = fhirContextInitializer.createClient(context, fhirUrl, accessToken);
			logger.info("Client after Initializing : {} ", client);
			
//			logger.info("jsonResource string reportingBundle  : {} " , r4Context.newJsonParser().encodeResourceToString(reportingBundle));
			Bundle responseBundle = fhirContextInitializer.submitProcessMessage(client, reportingBundle);
			logger.info("Fhir response after submit processMessage responseBundle.getId() : {} ", responseBundle.getId());
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
	
	private Bundle createReportingBundle(String profile) {
		Bundle returnBundle = new Bundle();

		returnBundle.setId(UUID.randomUUID().toString());
		returnBundle.setType(BundleType.MESSAGE);
		returnBundle.setMeta(getMeta(DEFAULT_VERSION, profile));
		returnBundle.setTimestamp(Date.from(Instant.now()));

		return returnBundle;
	}

	private static Meta getMeta(String version, String profile) {
		Meta m = new Meta();
		m.setVersionId(version);
		CanonicalType ct = new CanonicalType();
		ct.setValueAsString(profile);
		List<CanonicalType> profiles = new ArrayList<>();
		profiles.add(ct);
		m.setProfile(profiles);
		m.setLastUpdated(Date.from(Instant.now()));

		return m;
	}

	private MessageHeader createMessageHeader(Boolean cdaFlag, Bundle contentBundle, String fhirUrl) {
		MessageHeader header = new MessageHeader();

		header.setId(UUID.randomUUID().toString());
		header.setMeta(getMeta(DEFAULT_VERSION, MESSAGE_HEADER_PROFILE));

		// Add extensions
		Extension ext = new Extension();
		ext.setUrl(MESSAGE_PROCESSING_CATEGORY_EXT_URL);
		StringType st = new StringType();
		st.setValue(MESSAGE_PROCESSING_CATEGORY_CODE);
		ext.setValue(st);
		List<Extension> exts = new ArrayList<>();
		exts.add(ext);

		header.setExtension(exts);

		// Set message type.
		Coding c = new Coding();
		c.setSystem(MESSAGE_TYPE_URL);
		if (Boolean.TRUE.equals(cdaFlag)) {
			c.setCode(getMessageTypeString(MessageType.CDA_EICR_MESSAGE));
		} else {
			c.setCode(getMessageTypeString(MessageType.FHIR_REPORTABILITY_RESPONSE_MESSAGE));
		}

		header.setEvent(c);

		// set destination
		Set<UriType> dests = new HashSet<UriType>();
		UriType uriType = new UriType(fhirUrl);
		dests.add(uriType);

		List<MessageDestinationComponent> mdcs = new ArrayList<>();
		for (UriType i : dests) {
			MessageDestinationComponent mdc = new MessageDestinationComponent();
			mdc.setEndpoint(i.asStringValue());
			mdcs.add(mdc);
		}
		header.setDestination(mdcs);

		// Set source.
		MessageSourceComponent msgComp = new MessageSourceComponent();
		msgComp.setEndpoint(fhirUrl);
		header.setSource(msgComp);

		String triggerEvent = "";

		// Set Reason.
		CodeableConcept codeCpt = new CodeableConcept();
		Coding coding = new Coding();
		coding.setSystem(NAMED_EVENT_URL);
		coding.setCode(triggerEvent);
		codeCpt.addCoding(coding);
		header.setReason(codeCpt);

		// Add sender

		Organization org = getOrganization();

		if (org != null) {
			Reference orgRef = new Reference();
			orgRef.setResource(org);
			header.setSender(orgRef);
		}

		// Setup Message Header to Content Bundle Linkage.
		Reference ref = new Reference();
		ref.setReference(BUNDLE_REL_URL + contentBundle.getId());
		List<Reference> refs = new ArrayList<>();
		refs.add(ref);
		header.setFocus(refs);

		return header;
	}

	private static String getMessageTypeString(MessageType t) {

		if (t == MessageType.CANCER_REPORT_MESSAGE)
			return "cancer-report-message";
		else if (t == MessageType.HEP_C_REPORT_MESSAGE)
			return "hepc-report-message";
		else if (t == MessageType.HEALTHCARE_SURVEY_REPORT_MESSAGE)
			return "healthcare-survey-report-message";
		else if (t == MessageType.RESPNET_CASE_REPORT_MESSAGE)
			return "respnet-case-report-message";
		else if (t == MessageType.CDA_EICR_MESSAGE)
			return "CdaEicrMessage";
		else if (t == MessageType.EICR_CASE_REPORT_MESSAGE)
			return "eicr-case-report-message";
		else if (t == MessageType.CDA_REPORTABILITY_RESPONSE_MESSAGE)
			return "CdaReportabilityResponseMessage";
		else if (t == MessageType.FHIR_REPORTABILITY_RESPONSE_MESSAGE)
			return "FhirReportabilityResponseMessage";
		else
			return "message-report";
	}

	private enum MessageType {
		CANCER_REPORT_MESSAGE, HEP_C_REPORT_MESSAGE, HEALTHCARE_SURVEY_REPORT_MESSAGE, RESPNET_CASE_REPORT_MESSAGE,
		CDA_EICR_MESSAGE, EICR_CASE_REPORT_MESSAGE, CDA_REPORTABILITY_RESPONSE_MESSAGE,
		FHIR_REPORTABILITY_RESPONSE_MESSAGE, MESSAGE_REPORT,
	}

	private static Organization getOrganization() {
		Organization org = null;
		return org;
	}  
}
