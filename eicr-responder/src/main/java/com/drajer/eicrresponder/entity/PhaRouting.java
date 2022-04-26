package com.drajer.eicrresponder.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * @author Girish Rao 
 * pha routing entity object
 */
@Entity
@Table(name = "pha_routing")
@EntityListeners(AuditingEntityListener.class)
public class PhaRouting {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id; // Each pha routing will be given an auto-generated unique identifier when
						// stored

	@Column(name = "pha_agency_code", nullable = false)
	private String phaAgencyCode; // Save the agency code

	@Column(name = "receiver_protocol", nullable = false)
	private String receiverProtocol; // Save the receiver protocol

	@Column(name = "protocol_type", nullable = false)
	private String protocolType; // save protocol type

	@Column(name = "endpoint_url", nullable = false)
	private String endpointUrl; // Save the url

	@Column(name = "retry_count", nullable = false)
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

}
