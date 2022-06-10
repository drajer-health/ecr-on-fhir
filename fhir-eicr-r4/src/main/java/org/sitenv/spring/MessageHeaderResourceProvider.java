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
import org.sitenv.spring.model.MetaData;
import org.sitenv.spring.service.AmazonClientService;
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
	AmazonClientService amazonClientService;
	
	public MessageHeaderResourceProvider() {
		context = new AnnotationConfigApplicationContext(AppConfig.class);
		planDefinition = (PlanDefinitionService) context.getBean("PlanDefinitionService");
		bundleService = (BundleService) context.getBean("BundleService");
		communicationService = (CommunicationService) context.getBean("CommunicationService");
		amazonClientService = (AmazonClientService) context.getBean("AmazonClientService");
	}

	@Operation(name = "$process-message", idempotent = false)
	public Bundle processMessage(HttpServletRequest theServletRequest, RequestDetails theRequestDetails,
								 @OperationParam(name = "content", min = 1, max = 1) @Description(formalDefinition = "The message to process (or, if using asynchronous messaging, it may be a response message to accept)") Bundle theMessageToProcess) {
		logger.info("Validating the Bundle");
		Bundle bundle	 = theMessageToProcess;
		Bundle resourceBdl = new Bundle();
		OperationOutcome outcome = new OperationOutcome();
		MetaData metaData = new MetaData();

		boolean errorExists = false;
		try {
			String requestBdl="";

			for (BundleEntryComponent next : bundle.getEntry()) {
				if(next.getResource() instanceof MessageHeader){
					MessageHeader msgHeader	= (MessageHeader) next.getResource();
					metaData.setMessageId(((IdType)msgHeader.getIdElement()).getIdPart());
					metaData.setSenderUrl(msgHeader.getSource().getEndpoint());
				}
				if (next.getResource() instanceof Bundle) {
					resourceBdl = (Bundle) next.getResource();
					requestBdl  = r4Context.newJsonParser().encodeResourceToString(resourceBdl);
					//System.out.println("Bundle Entry Resource == > "+ requestBdl);
				}
			}

			Properties prop = fetchProperties();
			String validatorEndpoint = System.getProperty("validator.endpoint") == null ?  prop.getProperty("validator.endpoint") : System.getProperty("validator.endpoint");
			outcome = new CommonUtil().validateResource(resourceBdl,validatorEndpoint, r4Context);

			//Convert JSON to XML
			IParser ip = r4Context.newJsonParser(),
					op = r4Context.newXmlParser();
			IBaseResource ri = ip.parseResource(requestBdl);
			String output = op.setPrettyPrint(true).encodeResourceToString(ri);

			//System.out.println("XML Output === "+ "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+output );

			// write to s3
			try {
				// Write Meta Data
				amazonClientService.uploadMetaDataS3bucket(metaData.getMessageId(), metaData);

				//Check for Message Id and Error Handling
				amazonClientService.uploadBundle3bucket(metaData.getMessageId() , "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+output);
			}catch(Exception e) {
				e.printStackTrace();
			}

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
				bundle.setId(metaData.getMessageId());
				dafBundle.setEicrData(r4Context.newJsonParser().encodeResourceToString(bundle));
				dafBundle.setEicrDataProcessStatus("RECEIVED");
				dafBundle.setCreatedDate(new Date());
				bundleService.createBundle(dafBundle);
			}

				Bundle responseBundle = new Bundle();
			List<BundleEntryComponent> bundleEntryList = new ArrayList<>();
			BundleEntryComponent entryComp = new BundleEntryComponent();
			entryComp.setResource(outcome);
			bundleEntryList.add(entryComp);
			responseBundle.setEntry(bundleEntryList);
			return responseBundle;

		} catch (Exception e) {
			throw new UnprocessableEntityException("Error in Processing the Bundle");
		}
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
