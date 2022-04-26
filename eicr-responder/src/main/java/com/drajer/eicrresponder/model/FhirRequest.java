package com.drajer.eicrresponder.model;

import org.hl7.fhir.r4.model.Bundle;

/**
 * @author Girish Rao 
 * Fhir request object
 */
public class FhirRequest {

	private String fhirVersion;
	private String accessToken;
	private String fhirServerURL;
	private Bundle bundle;

	/**
	 * @return fhirVersion
	 */
	public String getFhirVersion() {
		return fhirVersion;
	}

	/**
	 * @param fhirVersion
	 */
	public void setFhirVersion(String fhirVersion) {
		this.fhirVersion = fhirVersion;
	}

	/**
	 * @return accessToken
	 */
	public String getAccessToken() {
		return accessToken;
	}

	/**
	 * @param accessToken
	 */
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	/**
	 * @return fhirServerURL
	 */
	public String getFhirServerURL() {
		return fhirServerURL;
	}

	/**
	 * @param fhirServerURL
	 */
	public void setFhirServerURL(String fhirServerURL) {
		this.fhirServerURL = fhirServerURL;
	}

	/**
	 * @return bundle
	 */
	public Bundle getBundle() {
		return bundle;
	}

	/**
	 * @param bundle
	 */
	public void setBundle(Bundle bundle) {
		this.bundle = bundle;
	}
}
