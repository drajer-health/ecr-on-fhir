package org.sitenv.spring;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.sitenv.spring.configuration.AppConfig;
import org.sitenv.spring.model.DafBundle;
import org.sitenv.spring.model.DafCommunication;
import org.sitenv.spring.service.BundleService;
import org.sitenv.spring.service.CommunicationService;
import org.sitenv.spring.service.PlanDefinitionService;
import org.sitenv.spring.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.util.ResourceUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class MessageHeaderResourceProvider {

	//need to read from conf file
	//private static final String validatorEndpoint = "http://ecr.drajer.com/fhir-eicr-validator/r4/resource/validate";

	protected FhirContext r4Context = FhirContext.forR4();

	private static final Logger logger = LoggerFactory.getLogger(MessageHeaderResourceProvider.class);

	AbstractApplicationContext context;
	PlanDefinitionService planDefinition;
	BundleService bundleService;
	CommunicationService communicationService;

	public MessageHeaderResourceProvider() {
		context = new AnnotationConfigApplicationContext(AppConfig.class);
		planDefinition = (PlanDefinitionService) context.getBean("PlanDefinitionService");
		bundleService = (BundleService) context.getBean("BundleService");
		communicationService = (CommunicationService) context.getBean("CommunicationService");
	}

	@Operation(name = "$process-message", idempotent = false)
	public Bundle processMessage(HttpServletRequest theServletRequest, RequestDetails theRequestDetails,
								 @OperationParam(name = "content", min = 1, max = 1) @Description(formalDefinition = "The message to process (or, if using asynchronous messaging, it may be a response message to accept)") Bundle theMessageToProcess) {
		logger.info("Validating the Bundle");
		Bundle bundle	 = theMessageToProcess;
		OperationOutcome outcome = new OperationOutcome();
		boolean errorExists = false;
		try {
			String request = r4Context.newJsonParser().encodeResourceToString(bundle);
			Properties prop = fetchProperties();
			String validatorEndpoint = prop.getProperty("validator.endpoint");
			outcome = new CommonUtil().validateResource(bundle,validatorEndpoint, r4Context);

			//Convert JSON to XML
			IParser ip = r4Context.newJsonParser(),
					op = r4Context.newXmlParser();
			IBaseResource ri = ip.parseResource(request);
			String output = op.setPrettyPrint(true).encodeResourceToString(ri);

			System.out.println("XML Output === "+ output );

			if (outcome.hasIssue()) {
				List<OperationOutcomeIssueComponent> issueCompList = outcome.getIssue();
				for (OperationOutcomeIssueComponent issueComp : issueCompList) {
					if (issueComp.getSeverity().equals(IssueSeverity.ERROR)) {
						errorExists = true;
					}
				}
			}
			if (!errorExists) {
				DafBundle dafBundle = new DafBundle();
				bundle.setId(getUUID());
				dafBundle.setEicrData(r4Context.newJsonParser().encodeResourceToString(bundle));
				//dafBundle.setEicrValidateResult(outcome.get);
				dafBundle.setEicrDataProcessStatus("RECEIVED");
				dafBundle.setCreatedDate(new Date());
				bundleService.createBundle(dafBundle);
				MessageHeader messageHeader = null;
				String patientId = null;
				String commId =null;
				for(BundleEntryComponent entryComp: bundle.getEntry()) {
					if(entryComp.getResource().getResourceType().name().equals("MessageHeader")) {
						messageHeader = (MessageHeader) entryComp.getResource();
						messageHeader.setId(getUUID());
						Meta meta = messageHeader.getMeta();
						meta.setLastUpdated(new Date());
						messageHeader.setMeta(meta);
					} else if(entryComp.getResource().getResourceType().name().equals("Bundle")) {
						Bundle innerBundle = (Bundle) entryComp.getResource();
						for(BundleEntryComponent bundleEntryComponent:innerBundle.getEntry()) {
							if(bundleEntryComponent.getResource().getResourceType().name().equals("Patient")) {
								patientId = bundleEntryComponent.getResource().getIdElement().getIdPart().toString();
							}
						}
					}
				}
				if(patientId!= null) {
					commId = constructAndSaveCommunication(patientId);
				}
				if(messageHeader == null) {
					messageHeader = constructMessageHeaderResource();
				}
				if(commId != null) {
					List<Reference> referenceList = new ArrayList<Reference>();
					Reference commRef = new Reference();
					commRef.setReference("Communication/"+commId);
					referenceList.add(commRef);
					messageHeader.setFocus(referenceList);
				}

				Bundle respbundle = new Bundle();
				respbundle.setId(getUUID());
				List<BundleEntryComponent> entryCompList = new ArrayList<>();
				BundleEntryComponent entryComp = new BundleEntryComponent();
				entryComp.setResource(messageHeader);
				entryCompList.add(entryComp);
				respbundle.setEntry(entryCompList);
				return respbundle;
			} else {
				Bundle responseBundle = new Bundle();
				List<BundleEntryComponent> bundleEntryList = new ArrayList<>();
				BundleEntryComponent entryComp = new BundleEntryComponent();
				entryComp.setResource(outcome);
				bundleEntryList.add(entryComp);
				responseBundle.setEntry(bundleEntryList);
				return responseBundle;
			}
		} catch (Exception e) {
			throw new UnprocessableEntityException("Error in Processing the Bundle");
		}
	}

	private MessageHeader constructMessageHeaderResource() {
		String message = "{\"resourceType\": \"MessageHeader\",\"id\": \"messageheader-example-reportheader\",\"meta\": {\"versionId\": \"1\",\"lastUpdated\": \"2020-11-29T02:03:28.045+00:00\",\"profile\": [\"http://hl7.org/fhir/us/medmorph/StructureDefinition/us-ph-messageheader\"]},\"extension\": [{\"url\": \"http://hl7.org/fhir/us/medmorph/StructureDefinition/ext-dataEncrypted\",\"valueBoolean\": false},{\"url\":\"http://hl7.org/fhir/us/medmorph/StructureDefinition/ext-messageProcessingCategory\",\"valueCode\": \"consequence\"}],\"eventCoding\": {\"system\": \"http://hl7.org/fhir/us/medmorph/CodeSystem/us-ph-messageheader-message-types\",\"code\": \"cancer-report-message\"},\"destination\": [{\"name\": \"PHA endpoint\",\"endpoint\": \"http://example.pha.org/fhir\"}],\"source\": {\"name\": \"Healthcare Organization\",\"software\": \"Backend Service App\",\"version\": \"3.1.45.AABB\",\"contact\": {\"system\": \"phone\",\"value\": \"+1 (917) 123 4567\"},\"endpoint\": \"http://example.healthcare.org/fhir\"},\"reason\": {\"coding\": [{\"system\": \"http://hl7.org/fhir/us/medmorph/CodeSystem/us-ph-triggerdefinition-namedevents\",\"code\": \"encounter-close\"}]}}";
		MessageHeader messageHeader = (MessageHeader) r4Context.newJsonParser().parseResource(message);
		messageHeader.setId(getUUID());
		return messageHeader;
	}

	private String constructAndSaveCommunication(String patientId) {
		String communication ="{\"resourceType\" : \"Communication\",\"meta\" : {\"versionId\" : \"1\",\"profile\" : [\"http://hl7.org/fhir/us/medmorph/StructureDefinition/us-ph-communication\"]},\"extension\" : [{\"url\" : \"http://hl7.org/fhir/us/medmorph/StructureDefinition/ext-responseMessageStatus\",\"valueCodeableConcept\" : {\"coding\" : [{\"system\" :\"http://hl7.org/fhir/us/medmorph/CodeSystem/us-ph-response-message-processing-status\",\"code\" : \"RRVS1\"}]}}],\"identifier\" : [{\"system\" : \"http://example.pha.org/\",\"value\" : \"12345\"}],\"status\" : \"completed\",\"category\" : [{\"coding\" : [{\"system\" : \"http://hl7.org/fhir/us/medmorph/CodeSystem/us-ph-messageheader-message-types\",\"code\" : \"cancer-response-message\"}]}],\"reasonCode\" : [{\"coding\" : [{\"system\" : \"http://hl7.org/fhir/us/medmorph/CodeSystem/us-ph-messageheader-message-types\",\"code\" : \"cancer-report-message\"}]}]}";
		Communication comm = (Communication) r4Context.newJsonParser().parseResource(communication);
		String commId = getUUID();
		comm.setId(commId);
		Meta meta = comm.getMeta();
		meta.setLastUpdated(new Date());
		comm.setMeta(meta);
		comm.setSubject(new Reference("Patient/"+patientId));
		DafCommunication dafCommunication = new DafCommunication();
		dafCommunication.setData(r4Context.newJsonParser().encodeResourceToString(comm));
		dafCommunication.setTimestamp(new Date());
		communicationService.createCommunication(dafCommunication);
		return commId;
	}

	public String getUUID() {
		UUID uuid = UUID.randomUUID();
		String randomUUID = uuid.toString();
		return randomUUID;
	}
	public static Properties fetchProperties(){
		Properties properties = new Properties();
		try {
			File file = ResourceUtils.getFile("classpath:application.properties");
			InputStream in = new FileInputStream(file);
			properties.load(in);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return properties;
	}

}
