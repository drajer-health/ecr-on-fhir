package org.sitenv.spring;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import jakarta.servlet.http.HttpServletRequest;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.sitenv.spring.configuration.AppConfig;
import org.sitenv.spring.model.DafBundle;
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

/**
 * Resource provider for handling FHIR MessageHeader resources.
 * Provides a custom $process-message operation to validate and process incoming FHIR bundles.
 */
public class MessageHeaderResourceProvider {

	protected FhirContext r4Context = FhirContext.forR4();
	private static final Logger logger = LoggerFactory.getLogger(MessageHeaderResourceProvider.class);

	private final AbstractApplicationContext context;
	private final PlanDefinitionService planDefinition;
	private final BundleService bundleService;
	private final CommunicationService communicationService;
	private final AmazonClientService amazonClientService;

	/**
	 * Default constructor initializing services from the application context.
	 */
	public MessageHeaderResourceProvider() {
		context = new AnnotationConfigApplicationContext(AppConfig.class);
		planDefinition = (PlanDefinitionService) context.getBean("PlanDefinitionService");
		bundleService = (BundleService) context.getBean("BundleService");
		communicationService = (CommunicationService) context.getBean("CommunicationService");
		amazonClientService = (AmazonClientService) context.getBean("AmazonClientService");
	}

	/**
	 * Custom FHIR operation $process-message for processing and validating FHIR bundles.
	 *
	 * @param theServletRequest The HTTP servlet request containing additional headers.
	 * @param theRequestDetails Details about the FHIR request.
	 * @param theMessageToProcess The FHIR bundle to process.
	 * @return A FHIR Bundle containing the processing results.
	 */
	@Operation(name = "$process-message", idempotent = false)
	public Bundle processMessage(HttpServletRequest theServletRequest, RequestDetails theRequestDetails,
								 @OperationParam(name = "content", min = 1, max = 1) Bundle theMessageToProcess) {
		logger.info("Validating the Bundle");

		Bundle bundle = theMessageToProcess;
		MetaData metaData = new MetaData();
		String persistenceId = theServletRequest.getHeader("persistenceId");

		Properties properties = fetchProperties();
		String validatorEndpoint = System.getProperty("validator.endpoint", properties.getProperty("validator.endpoint"));

		boolean errorExists = false;
		try {
			String requestBundleJson = "";
			String resourceProfile = "";
			Bundle resourceBundle = extractBundleDetails(bundle, metaData);

			if (resourceBundle != null) {
				requestBundleJson = r4Context.newJsonParser().encodeResourceToString(resourceBundle);

				if (resourceBundle.getMeta().hasProfile()) {
					resourceProfile = resourceBundle.getMeta().getProfile().get(0).asStringValue();
					validatorEndpoint += "?profile=" + resourceProfile;
				}
			}

			OperationOutcome outcome = new CommonUtil().validateResource(resourceBundle, validatorEndpoint, r4Context);
			processValidationOutcome(outcome, metaData, persistenceId, requestBundleJson, resourceBundle);
			return createResponseBundle(outcome);

		} catch (Exception e) {
			logger.error("Error in processing the bundle", e);
			throw new UnprocessableEntityException("Error in Processing the Bundle");
		}
	}

	/**
	 * Extracts details from the given bundle and updates metadata accordingly.
	 *
	 * @param bundle The FHIR bundle to process.
	 * @param metaData The metadata object to populate.
	 * @return The nested bundle resource, if found; null otherwise.
	 */
	private Bundle extractBundleDetails(Bundle bundle, MetaData metaData) {
		for (BundleEntryComponent entry : bundle.getEntry()) {
			if (entry.getResource() instanceof MessageHeader) {
				MessageHeader messageHeader = (MessageHeader) entry.getResource();
				metaData.setMessageId(messageHeader.getIdElement().getIdPart());
				metaData.setSenderUrl(messageHeader.getSource().getEndpoint());
			}
			if (entry.getResource() instanceof Bundle) {
				return (Bundle) entry.getResource();
			}
		}
		return null;
	}

	/**
	 * Processes the validation outcome, uploads metadata and the bundle to S3, and updates the database.
	 *
	 * @param outcome The validation outcome.
	 * @param metaData The metadata object.
	 * @param persistenceId The unique persistence ID for the bundle.
	 * @param requestBundleJson The bundle in JSON format.
	 * @param resourceBundle The resource bundle.
	 */
	private void processValidationOutcome(OperationOutcome outcome, MetaData metaData, String persistenceId,
										  String requestBundleJson, Bundle resourceBundle) {
		try {
			String bundleXml = convertJsonToXml(requestBundleJson);
			amazonClientService.uploadMetaDataS3bucket(persistenceId, metaData);
			amazonClientService.uploadBundle3bucket(persistenceId, bundleXml);
			logger.info("Uploaded metadata and bundle to S3.");

			if (!hasErrorIssues(outcome)) {
				DafBundle dafBundle = new DafBundle();
				dafBundle.setEicrData(r4Context.newJsonParser().encodeResourceToString(resourceBundle));
				dafBundle.setEicrDataProcessStatus("RECEIVED");
				dafBundle.setCreatedDate(new Date());
				bundleService.createBundle(dafBundle);
			}
		} catch (Exception e) {
			logger.error("Error in processing validation outcome", e);
		}
	}

	/**
	 * Converts a JSON representation of a bundle to XML.
	 *
	 * @param json The JSON string.
	 * @return The XML string.
	 */
	private String convertJsonToXml(String json) {
		IParser jsonParser = r4Context.newJsonParser();
		IParser xmlParser = r4Context.newXmlParser();
		IBaseResource resource = jsonParser.parseResource(json);
		return xmlParser.setPrettyPrint(true).encodeResourceToString(resource);
	}

	/**
	 * Checks if the given OperationOutcome contains any errors.
	 *
	 * @param outcome The validation outcome to check.
	 * @return True if there are error issues, false otherwise.
	 */
	private boolean hasErrorIssues(OperationOutcome outcome) {
		return outcome.getIssue().stream()
				.anyMatch(issue -> issue.getSeverity() == IssueSeverity.ERROR);
	}

	/**
	 * Creates a response bundle containing the given OperationOutcome.
	 *
	 * @param outcome The OperationOutcome to include in the response.
	 * @return The response bundle.
	 */
	private Bundle createResponseBundle(OperationOutcome outcome) {
		Bundle responseBundle = new Bundle();
		BundleEntryComponent entryComponent = new BundleEntryComponent();
		entryComponent.setResource(outcome);
		responseBundle.addEntry(entryComponent);
		return responseBundle;
	}

	/**
	 * Generates a UUID.
	 *
	 * @return A randomly generated UUID.
	 */
	public String getUUID() {
		return UUID.randomUUID().toString();
	}

	/**
	 * Fetches application properties from the classpath.
	 *
	 * @return The loaded properties.
	 */
	public static Properties fetchProperties() {
		Properties properties = new Properties();
		try (InputStream in = new FileInputStream(ResourceUtils.getFile("classpath:application.properties"))) {
			properties.load(in);
		} catch (IOException e) {
			logger.error("Error loading properties", e);
		}
		return properties;
	}
}
