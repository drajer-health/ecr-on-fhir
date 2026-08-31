package com.drajer.eicrfhirvalidator.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import org.hl7.fhir.r5.model.OperationOutcome;

/**
 * Service layer for validating FHIR resources, either via HAPI's built-in structural validator
 * or via the embedded {@code org.hl7.fhir.validation} {@code ValidationEngine} against a
 * specific implementation guide profile.
 */
public interface ResourceValidationService {

	/**
	 * Validates a resource using HAPI's default R4 structural validator (no specific profile).
	 *
	 * @param r4Context the R4 {@link FhirContext} to validate with
	 * @param validator the HAPI {@link FhirValidator} instance to use
	 * @param bodyStr the resource content as a JSON string
	 * @return the HAPI {@link ValidationResult}
	 */
	ValidationResult validateR4Resource(FhirContext r4Context, FhirValidator validator, String bodyStr);

	/**
	 * Validates a resource against the given implementation guide profile(s) using the embedded
	 * {@code ValidationEngine}.
	 *
	 * @param bodyStr the resource content as a JSON string
	 * @param profile the canonical URL of the profile to validate against (comma-separated for
	 *     multiple profiles)
	 * @return an {@link OperationOutcome} describing the validation results
	 * @throws Exception if validation cannot be performed
	 */
	OperationOutcome validate(String bodyStr, String profile) throws Exception;

}
