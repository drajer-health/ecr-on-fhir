package com.drajer.EicrFhirvalidator.service;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.StrictErrorHandler;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import com.drajer.EicrFhirvalidator.component.Validator;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResourceValidationServiceImpl implements ResourceValidationService {

	@Autowired
	private Validator validator;

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
