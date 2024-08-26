package com.drajer.eicrfhirvalidator.service;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.StrictErrorHandler;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import com.drajer.eicrfhirvalidator.component.EmbeddedHapiFhirValidator;
import com.drajer.eicrfhirvalidator.component.IFhirValidator;
import com.drajer.eicrfhirvalidator.exception.EicrException;
import com.drajer.eicrfhirvalidator.exception.FhirServerNotAvailableException;
import jakarta.annotation.PostConstruct;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.validation.ValidationEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ResourceValidationServiceImpl implements ResourceValidationService {

	private static final Logger logger = LoggerFactory.getLogger(ResourceValidationService.class);

	@Autowired
	ValidationEngine validationEngine;

	private IFhirValidator<String, OperationOutcome> fhirValidator;


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
		ValidationResult result  = fhirContext.newValidator().validateWithResult(bodyStr);
		//resource = fhirContext.newJsonParser().setParserErrorHandler(new StrictErrorHandler()).parseResource(bodyStr);
		//ValidationResult result = val.validateWithResult(resource);
		return result;
	}

	@PostConstruct
	public void setUp() {
		fhirValidator = configure();
	}

	public synchronized IFhirValidator<String, OperationOutcome> configure() {
		if (Objects.nonNull(fhirValidator)) {
			return fhirValidator;
		}
		fhirValidator = new EmbeddedHapiFhirValidator(validationEngine);
		logger.info("Fhir Validator is configured");
		return fhirValidator;
	}

	public OperationOutcome validate(String resourceData, String resourceProfile) {
		try {
			if (Objects.isNull(fhirValidator)) {
				logger.error("FHIR Server is not available: {}", resourceProfile);
				throw new FhirServerNotAvailableException("FHIR Server is not available for " + resourceProfile);
			}
			logger.debug("Profile: {}", resourceProfile);
			Long startTime = System.currentTimeMillis();
			OperationOutcome operationOutcome = fhirValidator.validate(resourceData, resourceProfile).orElse(null);
			logger.debug("{} Ends Level2 Fhir server validation Time Taken in milliseconds in resource validator: {}", resourceProfile,(System.currentTimeMillis()-startTime)/1000);

			return operationOutcome;
		} catch (EicrException e) {
			logger.debug("{}: {}", e.getClass().getName(), e.getMessage());
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
