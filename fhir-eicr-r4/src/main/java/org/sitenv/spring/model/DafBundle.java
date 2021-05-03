package org.sitenv.spring.model;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.sitenv.spring.configuration.JSONObjectUserType;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="eicr")
@TypeDefs({@TypeDef(name = "StringJsonObject", typeClass = JSONObjectUserType.class)})
public class DafBundle {
	@Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name="eicr_data")
	@Type(type = "StringJsonObject")
	private String eicrData;

	@Column(name="eicr_data_process_status")
	private String eicrDataProcessStatus;

	@Column(name="eicr_source_endpoint")
	private String eicrSourceEndpoint;

	@Column(name="eicr_response_data")
	@Type(type = "StringJsonObject")
	private String eicrResponseData;

	@Column(name="eicr_data_response_status")
	private String eicrDataResponseStatus;

	@Column(name="eicr_validate_result")
	@Type(type = "StringJsonObject")
	private String eicrValidateResult;

	@Column(name="response_validate_result")
	@Type(type = "StringJsonObject")
	private String responseValidateResult;

	@Column(name="created_ts")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate;

	@Column(name="last_updated_ts")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getEicrData() {
		return eicrData;
	}

	public void setEicrData(String eicrData) {
		this.eicrData = eicrData;
	}

	public String getEicrDataProcessStatus() {
		return eicrDataProcessStatus;
	}

	public void setEicrDataProcessStatus(String eicrDataProcessStatus) {
		this.eicrDataProcessStatus = eicrDataProcessStatus;
	}

	public String getEicrSourceEndpoint() {
		return eicrSourceEndpoint;
	}

	public void setEicrSourceEndpoint(String eicrSourceEndpoint) {
		this.eicrSourceEndpoint = eicrSourceEndpoint;
	}

	public String getEicrResponseData() {
		return eicrResponseData;
	}

	public void setEicrResponseData(String eicrResponseData) {
		this.eicrResponseData = eicrResponseData;
	}

	public String getEicrDataResponseStatus() {
		return eicrDataResponseStatus;
	}

	public void setEicrDataResponseStatus(String eicrDataResponseStatus) {
		this.eicrDataResponseStatus = eicrDataResponseStatus;
	}

	public String getEicrValidateResult() {
		return eicrValidateResult;
	}

	public void setEicrValidateResult(String eicrValidateResult) {
		this.eicrValidateResult = eicrValidateResult;
	}

	public String getResponseValidateResult() {
		return responseValidateResult;
	}

	public void setResponseValidateResult(String responseValidateResult) {
		this.responseValidateResult = responseValidateResult;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
}
