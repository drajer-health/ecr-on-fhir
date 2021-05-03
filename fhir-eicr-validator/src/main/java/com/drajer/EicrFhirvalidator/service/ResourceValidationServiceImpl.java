package com.drajer.EicrFhirvalidator.service;

//import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
//import org.hl7.fhir.r4.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.drajer.EicrFhirvalidator.component.Validator;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.StrictErrorHandler;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.ValidationResult;

@Service
public class ResourceValidationServiceImpl implements ResourceValidationService {

	@Autowired
	private Validator validator;
	/**
	 * Validates DSTU2 resource
	 * 
	 * @param fhirContext
	 * @param val
	 * @param bodyStr
	 * @return
	 */
	@Override
	public ValidationResult validateDSTU2Resource(FhirContext fhirContext, FhirValidator val, String bodyStr) {
		IBaseResource resource = null;
			// Parse that JSON string encoding)
			resource = fhirContext.newJsonParser().setParserErrorHandler(new StrictErrorHandler())
					.parseResource(bodyStr);
			ValidationResult result = val.validateWithResult(resource);
		return result;
	}

	/**
	 * Validates STU3 resource
	 * 
	 * @param fhirContext
	 * @param val
	 * @param bodyStr
	 * @return
	 */
	@Override
	public ValidationResult validateSTU3Resource(FhirContext fhirContext, FhirValidator val, String bodyStr) {
		IBaseResource resource = null;
		// Parse that JSON string encoding)
		resource = fhirContext.newJsonParser().setParserErrorHandler(new StrictErrorHandler()).parseResource(bodyStr);
		ValidationResult result = val.validateWithResult(resource);
		return result;
	}

	/**
	 * Validates r4 resources
	 * 
	 * @param fhirContext
	 * @param val
	 * @param bodyStr
	 * @return
	 */
	public ValidationResult validateR4Resource(FhirContext fhirContext, FhirValidator val, String bodyStr) {
		IBaseResource resource = null;
		resource = fhirContext.newJsonParser().setParserErrorHandler(new StrictErrorHandler()).parseResource(bodyStr);
		ValidationResult result = val.validateWithResult(resource);
		return result;
	}

	/**
	 * Validates r4 resources based on us-core profile
	 */
	@Override
	public OperationOutcome validate(byte[] resource, String profile) throws Exception{
		return validator.validate(resource, profile);
	}
}
