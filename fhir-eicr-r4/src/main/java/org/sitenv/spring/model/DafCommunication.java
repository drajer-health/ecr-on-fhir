package org.sitenv.spring.model;

import org.hibernate.annotations.Type;
import org.sitenv.spring.configuration.JSONObjectUserType;

import jakarta.persistence.*;
import java.util.Date;

/**
 * JPA entity mapping the {@code communication} table, storing a FHIR
 * Communication resource as raw JSON text in {@code data} via
 * {@link JSONObjectUserType}.
 */
@Entity
@Table(name="communication")
public class DafCommunication {
	@Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name="data")
	@Type(JSONObjectUserType.class)
	private String data;
	
	@Column(name="last_updated_ts")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
}
