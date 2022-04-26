package com.drajer.eicrresponder.model;

import java.util.List;

public class MetaData {

	private String messageId;
	private String senderUrl;
	private List<Jurisdiction> jurisdictions;

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getSenderUrl() {
		return senderUrl;
	}

	public void setSenderUrl(String senderUrl) {
		this.senderUrl = senderUrl;
	}

	public List<Jurisdiction> getJurisdictions() {
		return jurisdictions;
	}

	public void setJurisdictions(List<Jurisdiction> jurisdictions) {
		this.jurisdictions = jurisdictions;
	}

	@Override
	public String toString() {
		return "MetaData [messageId=" + messageId + ", senderUrl=" + senderUrl + ", jurisdictions=" + jurisdictions
				+ "]";
	}

}
