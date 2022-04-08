package com.drajer.eicrresponder.model;

public class PhaRoutingResponse {
	private long id;
	private String phaAgencyCode; // Save the agency code
	private String receiverProtocol; // Save the receiver protocol
	private String protocolType; // save protocol type
	private String endpointUrl; // Save the url
	private long retryCount; // Save the retry count

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getPhaAgencyCode() {
		return phaAgencyCode;
	}

	public void setPhaAgencyCode(String phaAgencyCode) {
		this.phaAgencyCode = phaAgencyCode;
	}

	public String getReceiverProtocol() {
		return receiverProtocol;
	}

	public void setReceiverProtocol(String receiverProtocol) {
		this.receiverProtocol = receiverProtocol;
	}

	public String getProtocolType() {
		return protocolType;
	}

	public void setProtocolType(String protocolType) {
		this.protocolType = protocolType;
	}

	public String getEndpointUrl() {
		return endpointUrl;
	}

	public void setEndpointUrl(String endpointUrl) {
		this.endpointUrl = endpointUrl;
	}

	public long getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(long retryCount) {
		this.retryCount = retryCount;
	}

	@Override
	public String toString() {
		return "PhaRoutingResponse [id=" + id + ", phaAgencyCode=" + phaAgencyCode + ", receiverProtocol="
				+ receiverProtocol + ", protocolType=" + protocolType + ", endpointUrl=" + endpointUrl + ", retryCount="
				+ retryCount + "]";
	}

}
