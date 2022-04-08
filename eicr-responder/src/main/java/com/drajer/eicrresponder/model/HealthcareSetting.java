package com.drajer.eicrresponder.model;

public class HealthcareSetting {
	private String fhirServerBaseURL;
	private String clientId;
	private String clientSecret;
	private String fhirVersion;
	private String tokenUrl;
	private String scopes;
	private Boolean requireAud = false;
	private String authType;

	public String getFhirServerBaseURL() {
		return fhirServerBaseURL;
	}

	public void setFhirServerBaseURL(String fhirServerBaseURL) {
		this.fhirServerBaseURL = fhirServerBaseURL;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getFhirVersion() {
		return fhirVersion;
	}

	public void setFhirVersion(String fhirVersion) {
		this.fhirVersion = fhirVersion;
	}

	public String getTokenUrl() {
		return tokenUrl;
	}

	public void setTokenUrl(String tokenUrl) {
		this.tokenUrl = tokenUrl;
	}

	public String getScopes() {
		return scopes;
	}

	public void setScopes(String scopes) {
		this.scopes = scopes;
	}

	public Boolean getRequireAud() {
		return requireAud;
	}

	public void setRequireAud(Boolean requireAud) {
		this.requireAud = requireAud;
	}

	public String getAuthType() {
		return authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}

}
