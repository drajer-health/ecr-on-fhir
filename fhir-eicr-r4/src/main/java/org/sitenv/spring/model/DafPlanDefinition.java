package org.sitenv.spring.model;

import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.hibernate.annotations.Type;
import org.sitenv.spring.configuration.JSONObjectUserType;

/**
 * JPA entity mapping the {@code plandefinition} table, storing a FHIR
 * PlanDefinition resource as raw JSON text in {@code data} via
 * {@link JSONObjectUserType}.
 */
@Entity
@Table(name = "plandefinition")
public class DafPlanDefinition {
  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  @Column(name = "data")
  @Type(JSONObjectUserType.class)
  private String data;
  
  @Column(name = "last_updated_ts")
  @Temporal(TemporalType.TIMESTAMP)
  private Date timestamp;
  
  public Integer getId() {
    return this.id;
  }
  
  public void setId(Integer id) {
    this.id = id;
  }
  
  public String getData() {
    return this.data;
  }
  
  public void setData(String data) {
    this.data = data;
  }
  
  public Date getTimestamp() {
    return this.timestamp;
  }
  
  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }
}
