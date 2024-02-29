package com.drajer.EicrFhirvalidator.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import org.hl7.fhir.r5.model.OperationOutcome;

public interface ResourceValidationService {

	ValidationResult validateR4Resource(FhirContext r4Context, FhirValidator validator, String bodyStr);

	OperationOutcome validate(byte[] resource, String profile) throws Exception;
	
}
