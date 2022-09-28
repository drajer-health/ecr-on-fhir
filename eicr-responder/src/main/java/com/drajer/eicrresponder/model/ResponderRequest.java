package com.drajer.eicrresponder.model;

import java.util.List;

/**
 * @author Girish Rao 
 * Responder request object
 */
public class ResponderRequest {
	private Object eicrObject;
	private Object rrObject;
	private String fhirPostURL;
	private List<Jurisdiction> phaJurisdiction;
	private long retryCount;
	private MetaData metadata;
	private String eicrCdaXml;
	private String rrCdaXml;
	private String eicrFhirXml;
	private String rrFhirXml;
	
	public String getEicrCdaXml() {
		return eicrCdaXml;
	}

	public void setEicrCdaXml(String eicrCdaXml) {
		this.eicrCdaXml = eicrCdaXml;
	}

	public String getRrCdaXml() {
		return rrCdaXml;
	}

	public void setRrCdaXml(String rrCdaXml) {
		this.rrCdaXml = rrCdaXml;
	}

	public Object getEicrObject() {
		return eicrObject;
	}

	public void setEicrObject(Object eicrObject) {
		this.eicrObject = eicrObject;
	}

	public Object getRrObject() {
		return rrObject;
	}

	public void setRrObject(Object rrObject) {
		this.rrObject = rrObject;
	}

	public String getFhirPostURL() {
		return fhirPostURL;
	}

	public void setFhirPostURL(String fhirPostURL) {
		this.fhirPostURL = fhirPostURL;
	}

	public List<Jurisdiction> getPhaJurisdiction() {
		return phaJurisdiction;
	}

	public void setPhaJurisdiction(List<Jurisdiction> phaJurisdiction) {
		this.phaJurisdiction = phaJurisdiction;
	}

	public long getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(long retryCount) {
		this.retryCount = retryCount;
	}

	public MetaData getMetadata() {
		return metadata;
	}

	public void setMetadata(MetaData metadata) {
		this.metadata = metadata;
	}

	public String getEicrFhirXml() {
		return eicrFhirXml;
	}

	public void setEicrFhirXml(String eicrFhirXml) {
		this.eicrFhirXml = eicrFhirXml;
	}

	public String getRrFhirXml() {
		return rrFhirXml;
	}

	public void setRrFhirXml(String rrFhirXml) {
		this.rrFhirXml = rrFhirXml;
	}

	@Override
	public String toString() {
		return "ResponderRequest [eicrObject=" + eicrObject + ", rrObject=" + rrObject + ", fhirPostURL=" + fhirPostURL
				+ ", phaJurisdiction=" + phaJurisdiction + ", retryCount=" + retryCount + ", metadata=" + metadata
				+ ", eicrCdaXml=" + eicrCdaXml + ", rrCdaXml=" + rrCdaXml + ", eicrFhirXml=" + eicrFhirXml
				+ ", rrFhirXml=" + rrFhirXml + "]";
	}

}
