package com.drajer.eicrfhirvalidator.configuration;

import ca.uhn.fhir.context.FhirContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.drajer.eicrfhirvalidator")
public class FhirConfig {

    @Bean
    public FhirContext fhirContext() {
        return FhirContext.forR4(); // Or FhirContext.forR5(), depending on the FHIR version you are using
    }

}
