package org.sitenv.spring;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.sitenv.spring.configuration.AppConfig;
import org.sitenv.spring.model.DafBundle;
import org.sitenv.spring.model.MetaData;
import org.sitenv.spring.service.AmazonClientService;
import org.sitenv.spring.service.BundleService;
import org.sitenv.spring.service.CommunicationService;
import org.sitenv.spring.service.PlanDefinitionService;
import org.sitenv.spring.util.CommonUtil;
import org.sitenv.spring.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.ResourceUtils;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import jakarta.servlet.http.HttpServletRequest;

public class MessageHeaderResourceProvider {

	// need to read from conf file
	// private static final String validatorEndpoint =
	// "http://ecr.drajer.com/fhir-eicr-validator/r4/resource/validate";

	protected FhirContext r4Context = FhirContext.forR4();

	private static final Logger logger = LoggerFactory.getLogger(MessageHeaderResourceProvider.class);

	AbstractApplicationContext context;
	PlanDefinitionService planDefinition;
	BundleService bundleService;
	CommunicationService communicationService;
	AmazonClientService amazonClientService;
	Environment environment;

	public MessageHeaderResourceProvider() {
		context = new AnnotationConfigApplicationContext(AppConfig.class);
		planDefinition = (PlanDefinitionService) context.getBean("PlanDefinitionService");
		bundleService = (BundleService) context.getBean("BundleService");
		communicationService = (CommunicationService) context.getBean("CommunicationService");
		amazonClientService = (AmazonClientService) context.getBean("AmazonClientService");
		environment = (Environment) context.getBean(Environment.class);
	}

	@Operation(name = "$process-message", idempotent = false)
	public Bundle processMessage(HttpServletRequest theServletRequest, RequestDetails theRequestDetails,
			@OperationParam(name = "content", min = 1, max = 1) @Description(formalDefinition = "The message to process (or, if using asynchronous messaging, it may be a response message to accept)") Bundle theMessageToProcess) {
		logger.info("Validating the Bundle");
		IParser jsonParser = r4Context.newJsonParser();

		Bundle bundle = theMessageToProcess;
		Bundle resourceBdl = new Bundle();
		OperationOutcome outcome = new OperationOutcome();
		MetaData metaData = new MetaData();

// 		Get validator endpoint
		Properties prop = fetchProperties();
		String validatorEndpoint = System.getProperty("validator.endpoint") == null
				? prop.getProperty("validator.endpoint")
				: System.getProperty("validator.endpoint");

		boolean errorExists = false;
		try {
			String requestBdl = "";
			String resProfile = "";

			for (BundleEntryComponent next : bundle.getEntry()) {
				if (next.getResource() instanceof MessageHeader) {
					MessageHeader msgHeader = (MessageHeader) next.getResource();
					metaData.setMessageId(((IdType) msgHeader.getIdElement()).getIdPart());
					metaData.setSenderUrl(msgHeader.getSource().getEndpoint());
				}
				if (next.getResource() instanceof Bundle) {
					resourceBdl = (Bundle) next.getResource();
					requestBdl = r4Context.newJsonParser().encodeResourceToString(resourceBdl);

					Meta resMeta = resourceBdl.getMeta();
					if (resMeta.hasProfile()) {
						CanonicalType canonicalProfileType = resMeta.getProfile().get(0);
						resProfile = canonicalProfileType.asStringValue();
						validatorEndpoint = validatorEndpoint + "?profile=" + resProfile;
					}

					// System.out.println("Bundle Entry Resource == > "+ requestBdl);
				}
			}

			outcome = new CommonUtil().validateResource(resourceBdl, validatorEndpoint, r4Context);

			// Convert JSON to XML
			IParser ip = r4Context.newJsonParser(), op = r4Context.newXmlParser();
			IBaseResource ri = ip.parseResource(requestBdl);
			String output = op.setPrettyPrint(true).encodeResourceToString(ri);

			// System.out.println("XML Output === "+ "<?xml version=\"1.0\"
			// encoding=\"UTF-8\"?>\n"+output );

			// write to s3
			String s3StorageRequiredStr = prop.getProperty("s3Storage.required");
			boolean s3StorageRequired = Boolean.parseBoolean(s3StorageRequiredStr);

			if (s3StorageRequired) {

				try {

					amazonClientService.uploadMetaDataS3bucket(metaData.getMessageId(), metaData);

					// Check for Message Id and Error Handling
					amazonClientService.uploadBundle3bucket(metaData.getMessageId(),
							"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + output);

				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println("Done Writing MetaData and resource Bundle to S3 ");

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

			// create

			if (!s3StorageRequired) {
				String outputBundle = createAndSerializeBundle(responseBundle, jsonParser);

				String directoryPath = environment
						.getProperty(errorExists ? "process.message.output.failure.path":"process.message.output.success.path");

				if (directoryPath == null || directoryPath.isEmpty()) {
					throw new UnprocessableEntityException("No Process Message Outcome Storage path found ");
				}

				Path outputDir = ensureDirectoryExists(directoryPath);
				String outputFilePath = buildOutputFilePath(outputDir, "process-message");

				FileUtil.writeFileLocalJson(outputBundle, outputFilePath);
				System.out.println("Process Message Outcome  written to file: " + outputFilePath);
			}

			return responseBundle;

		} catch (Exception e) {
			e.printStackTrace();
			throw new UnprocessableEntityException("Error in Processing the Bundle");
		}
	}

	public String getUUID() {
		UUID uuid = UUID.randomUUID();
		String randomUUID = uuid.toString();
		return randomUUID;
	}

	public static Properties fetchProperties() {
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

	// Create the response bundle and serialize to JSON
	private String createAndSerializeBundle(Bundle outcome, IParser jsonParser) {

		return jsonParser.setPrettyPrint(true).encodeResourceToString(outcome);
	}

	private Path ensureDirectoryExists(String directoryPath) throws IOException {
		Path path = Paths.get(directoryPath);
		if (!Files.exists(path)) {
			Files.createDirectories(path);
		}
		return path;
	}

	private String buildOutputFilePath(Path directoryPath, String filePrefix) {
		return directoryPath.resolve(filePrefix + System.currentTimeMillis() + ".json").toString();
	}

}
