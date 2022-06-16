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
 * Received Eicr Data Log
 */
@Entity
@Table(name = "responder_eicr_data_log")
@EntityListeners(AuditingEntityListener.class)
public class ResponderDataLog {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id; // an auto-generated unique identifier

	@Column(name = "eicr_id", nullable = false)
	private String eicrId; // eicr id

	@Column(name = "eicr_received_datatime", nullable = false)
	private java.sql.Timestamp eicrReceivedDatatime; // eicr received data time

	@Column(name = "processed_status", nullable = false)
	private String processedStatus; // status

	@Column(name = "endpoint_url", nullable = false)
	private String endpointUrl; // Save the url

	@Column(name = "response_message_pha")
	private String responseMessage; // response message

	@Column(name = "response_message_fhir")
	private String responseMessageFhir; // response message	

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getEicrId() {
		return eicrId;
	}

	public void setEicrId(String eicrId) {
		this.eicrId = eicrId;
	}

	public java.sql.Timestamp getEicrReceivedDatatime() {
		return eicrReceivedDatatime;
	}

	public void setEicrReceivedDatatime(java.sql.Timestamp eicrReceivedDatatime) {
		this.eicrReceivedDatatime = eicrReceivedDatatime;
	}

	public String getProcessedStatus() {
		return processedStatus;
	}

	public void setProcessedStatus(String processedStatus) {
		this.processedStatus = processedStatus;
	}

	public String getEndpointUrl() {
		return endpointUrl;
	}

	public void setEndpointUrl(String endpointUrl) {
		this.endpointUrl = endpointUrl;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	public String getResponseMessageFhir() {
		return responseMessageFhir;
	}

	public void setResponseMessageFhir(String responseMessageFhir) {
		this.responseMessageFhir = responseMessageFhir;
	}

}
