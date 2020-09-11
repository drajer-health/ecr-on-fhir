package com.drajer.fhir.fhirvalidator.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import ca.uhn.fhir.context.FhirContext;

@Configuration
@ComponentScan(basePackages = { "com.drajer.fhir.fhirvalidator" })
public class FhirConfiguration {

	@Bean(name = "dstu2FhirContext")
	public FhirContext getDstu2FhirContext() {
		FhirContext dstu2FhirContext = FhirContext.forDstu2();
		return dstu2FhirContext;
	}

	@Bean(name = "dstu2HL7FhirContext")
	public FhirContext getDstu2HL7FhirContext() {
		FhirContext dstu2HL7FhirContext = FhirContext.forDstu2Hl7Org();
		return dstu2HL7FhirContext;
	}

	@Bean(name = "dstu3FhirContext")
	public FhirContext getDstu3FhirContext() {
		FhirContext dstu2FhirContext = FhirContext.forDstu3();
		return dstu2FhirContext;
	}

	@Bean(name = "r4FhirContext")
	public FhirContext getR4FhirContext() {
		FhirContext dstu2FhirContext = FhirContext.forR4();
		return dstu2FhirContext;
	}

	@Bean(name = "r5FhirContext")
	public FhirContext getR5FhirContext() {
		FhirContext r5FhirContext = FhirContext.forR5();
		return r5FhirContext;
	}
}
