package com.drajer.eicrfhirvalidator.configuration;

import ca.uhn.fhir.context.FhirContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration providing the {@link FhirContext} beans used across the application, and
 * enabling component scanning for the {@code com.drajer.eicrfhirvalidator} package.
 */
@Configuration
@ComponentScan(basePackages = "com.drajer.eicrfhirvalidator")
public class FhirConfig {

    /**
     * Default (unqualified) FHIR context, configured for R4.
     *
     * @return a new R4 {@link FhirContext}
     */
    @Bean
    public FhirContext fhirContext() {
        return FhirContext.forR4(); // Or FhirContext.forR5(), depending on the FHIR version you are using
    }

    /**
     * FHIR R4 context, injected via the {@code r4FhirContext} qualifier.
     *
     * @return a new R4 {@link FhirContext}
     */
    @Bean(name = "r4FhirContext")
    public FhirContext getR4FhirContext() {
        FhirContext dstu2FhirContext = FhirContext.forR4();
        return dstu2FhirContext;
    }

    /**
     * FHIR R5 context, injected via the {@code r5FhirContext} qualifier.
     *
     * @return a new R5 {@link FhirContext}
     */
    @Bean(name = "r5FhirContext")
    public FhirContext getR5FhirContext() {
        FhirContext r5FhirContext = FhirContext.forR5();
        return r5FhirContext;
    }

}
