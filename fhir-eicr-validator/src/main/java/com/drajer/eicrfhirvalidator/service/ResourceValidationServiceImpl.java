package com.drajer.eicrfhirvalidator.service;


import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import com.drajer.eicrfhirvalidator.component.EmbeddedHapiFhirValidator;
import com.drajer.eicrfhirvalidator.component.IFhirValidator;
import com.drajer.eicrfhirvalidator.component.Validator;
import com.drajer.eicrfhirvalidator.exception.EicrException;
import com.drajer.eicrfhirvalidator.exception.FhirServerNotAvailableException;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.validation.ValidationEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Default {@link ResourceValidationService} implementation, backed by an
 * {@link EmbeddedHapiFhirValidator} wrapping the {@link ValidationEngine} that {@link Validator}
 * loads in the background.
 */
@Service
public class ResourceValidationServiceImpl implements ResourceValidationService {

	private static final Logger logger = LoggerFactory.getLogger(ResourceValidationService.class);

	@Autowired
	Validator validator;

	private volatile IFhirValidator<String, OperationOutcome> fhirValidator;

	/**
	 * {@inheritDoc}
	 *
	 * @param val the HAPI {@link FhirValidator} instance to use
	 * @param bodyStr the resource content as a JSON string
	 * @return the HAPI {@link ValidationResult}
	 */
	public ValidationResult validateR4Resource(FhirValidator val, String bodyStr) {
		return val.validateWithResult(bodyStr);
	}


	/**
	 * Lazily creates (and caches) the {@link EmbeddedHapiFhirValidator} wrapping the
	 * {@link ValidationEngine}, waiting for the engine if it is still loading.
	 *
	 * @return the configured {@link IFhirValidator} instance
	 * @throws FhirServerNotAvailableException if the engine is disabled, failed to load, or is
	 *     still loading after the configured wait
	 */
	public IFhirValidator<String, OperationOutcome> configure() {
		if (Objects.nonNull(fhirValidator)) {
			return fhirValidator;
		}
		// Wait outside the lock so a slow engine load does not serialize callers on this monitor
		ValidationEngine validationEngine = validator.awaitValidationEngine();
		synchronized (this) {
			if (Objects.isNull(fhirValidator)) {
				fhirValidator = new EmbeddedHapiFhirValidator(validationEngine);
				logger.info("Fhir Validator is configured");
			}
			return fhirValidator;
		}
	}



	/**
	 * {@inheritDoc}
	 *
	 * @throws FhirServerNotAvailableException if the embedded validator has not been configured
	 * @throws RuntimeException wrapping any other failure during validation
	 */
	public OperationOutcome validate(String bodyStr, String resourceProfile) {
		try {
			IFhirValidator<String, OperationOutcome> fhirValidator;
			try {
				fhirValidator = configure();
			} catch (FhirServerNotAvailableException e) {
				logger.error("FHIR Server is not available: {}: {}", resourceProfile, e.getMessage());
				throw e;
			}
			logger.debug("Profile: {}", resourceProfile);
			Long startTime = System.currentTimeMillis();
			OperationOutcome operationOutcome = fhirValidator.validate(bodyStr, resourceProfile).orElse(null);
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
