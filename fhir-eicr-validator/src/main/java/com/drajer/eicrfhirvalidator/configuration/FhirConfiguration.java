package com.drajer.eicrfhirvalidator.configuration;

import ca.uhn.fhir.context.FhirContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = { "com.drajer.eicrfhirvalidator" })
public class FhirConfiguration {

	@Bean(name = "r4FhirContext")
	public FhirContext getR4FhirContext() {
		FhirContext r4FhirContext = FhirContext.forR4();
		return r4FhirContext;
	}

	@Bean(name = "r5FhirContext")
	public FhirContext getR5FhirContext() {
		FhirContext r5FhirContext = FhirContext.forR5();
		return r5FhirContext;
	}

}
