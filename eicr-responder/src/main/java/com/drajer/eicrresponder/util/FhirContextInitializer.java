package com.drajer.eicrresponder.util;

import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;

/**
 * @author Girish Rao
 *
 */
@Component
public class FhirContextInitializer {

	private static final String R4 = "R4";

	private static final Logger logger = LoggerFactory.getLogger(FhirContextInitializer.class);

	/**
	 * Get FhirContext appropriate to fhirVersion
	 *
	 * @param fhirVersion The FHIR Version to use, either as a fhir version or a
	 *                    package name.
	 * @return The appropriate FhirContext to use for the server
	 */
	public FhirContext getFhirContext(String fhirVersion) {
		switch (fhirVersion) {
		case R4:
			return FhirContext.forR4();
		default:
			return FhirContext.forDstu2();
		}
	}

	/**
	 * Creates a GenericClient with standard intercepter used throughout the
	 * services.
	 *
	 * @param url the base URL of the FHIR server to connect to
	 * @param accessToken the name of the key to use to generate the token
	 * @return a Generic Client
	 */
	public IGenericClient createClient(FhirContext context, String url, String accessToken) {
		logger.trace("Initializing the Client");
		IGenericClient client = context.newRestfulGenericClient(url);
		context.getRestfulClientFactory().setSocketTimeout(30 * 1000);
		client.registerInterceptor(new BearerTokenAuthInterceptor(accessToken));
		client.setEncoding(EncodingEnum.JSON);
		if (logger.isDebugEnabled()) {
			client.registerInterceptor(new LoggingInterceptor(true));
		}
		logger.trace("Initialized the Client");
		return client;
	}

	/**
	 * New operation for sending messages services.
	 *
	 * @param GenericClient generic client
	 * @param bundle        bundle to send
	 * @return a Bundle
	 */
	public Bundle submitProcessMessage(IGenericClient client, Bundle bundle) {
		Bundle response = client.operation().processMessage().setMessageBundle(bundle).asynchronous(Bundle.class)
				.execute();
		return response;
	}
}
