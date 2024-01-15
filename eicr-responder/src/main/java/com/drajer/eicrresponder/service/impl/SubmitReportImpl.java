package com.drajer.eicrresponder.service.impl;

import java.io.InputStream;
import java.util.Properties;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.drajer.eicrresponder.util.FhirContextInitializer;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.gclient.IOperationProcessMsgMode;

@Service
public class SubmitReportImpl {
	private final Logger logger = LoggerFactory.getLogger(SubmitReportImpl.class);

	private static final FhirContext context = FhirContext.forR4();

	/**
	 * The EHR Authorization Service class enables the BSA to get an access token.
	 */
	@Qualifier("backendauth")
	@Autowired
	BackendAuthorizationServiceImpl backendAuthorizationService;

	@Autowired
	FhirContextInitializer fhirContextInitializer;

	public String submitFhirOutput(Bundle bundleToSubmit, String fhirServerBaseURL) {
		return submitResources(bundleToSubmit, fhirServerBaseURL);
	}

	private String submitResources(Bundle bundleToSubmit, String fhirServerBaseURL) {
		String rtnMessage ="Error calling $process-message endpoin";
		JSONObject jsonObj = backendAuthorizationService.getAuthorizationToken(fhirServerBaseURL);
		String token = null;

		if (jsonObj != null) {
			token = jsonObj.getString("access_token");
			logger.info(" Successfully retrieve token {}", token);
		} else {
			logger.error(" Unable to retrieve access token for PHA: {}", fhirServerBaseURL);
		}

		Properties headers = new Properties();
		try (InputStream inputStream = SubmitReportImpl.class.getClassLoader()
				.getResourceAsStream("report-headers.properties")) {
			headers.load(inputStream);
		} catch (Exception ex) {
			logger.error("Error while loading report headers from Properties File ");
		}

		// resources to be submitted

		IGenericClient client = fhirContextInitializer.createClient(context, fhirServerBaseURL, token
		// , data.getxRequestId()
		);

		context.getRestfulClientFactory().setSocketTimeout(120 * 1000);
		context.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);

		// All submissions are expected to be bundles

		IOperationProcessMsgMode<IBaseResource> operation = client.operation().processMessage()
				.setMessageBundle(bundleToSubmit);

		headers.forEach((key, value) -> operation.withAdditionalHeader((String) key, (String) value));

		Bundle responseBundle;
		Object response = null;
		try {
			logger.info(" Trying to invoke $process-message");
			response = operation.encodedJson().execute();
			responseBundle = (Bundle) response;
		} catch (RuntimeException re) {
			logger.error("Error calling $process-message endpoint", re);
			logger.info("Response Object was {}", response);
			return rtnMessage;
		}

		logger.debug("Response is {}", responseBundle);
		if (responseBundle != null) {
			rtnMessage = "Bundle submitted to PHA";
			logger.info(" Adding Response Bundle to output using id {}", responseBundle.getId());
		} else {
			rtnMessage = "Response Bundle is NULL";
			logger.error("Response BUNDLE IS NULL");
		}
		return rtnMessage;
	}

}
