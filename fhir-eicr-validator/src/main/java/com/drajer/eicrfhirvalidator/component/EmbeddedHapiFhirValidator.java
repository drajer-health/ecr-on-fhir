package com.drajer.eicrfhirvalidator.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r5.elementmodel.Manager;
import org.hl7.fhir.r5.formats.FormatUtilities;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.hl7.fhir.validation.ValidationEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drajer.eicrfhirvalidator.configuration.ValidationEngineFactory;
import com.drajer.eicrfhirvalidator.exception.EicrException;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

/**
 * Class EmbeddedHapiFhirValidator TODO
 *
 * @author Drajer LLC
 * @since 27-04-2023
 */
public class EmbeddedHapiFhirValidator implements IFhirValidator<String, OperationOutcome> {

	private static final Logger logger = LoggerFactory.getLogger(EmbeddedHapiFhirValidator.class);
	private static final String COMMA_SEPARATOR = ",";

	private final ValidationEngineFactory validationEngineFactory;
	private FhirContext r4Context;

	public EmbeddedHapiFhirValidator(ValidationEngineFactory validationEngineFactory, FhirContext r4Context) {
		this.r4Context = r4Context;
		this.validationEngineFactory = validationEngineFactory;
	}

	@Override
	public Optional<OperationOutcome> validate(String resourceData, String profiles, String validatorMessageType)
			throws EicrException {

		List<String> resourceProfiles = profiles.contains(COMMA_SEPARATOR)
				? Arrays.asList(profiles.split(COMMA_SEPARATOR))
				: List.of(profiles);

		if (validatorMessageType == null || validatorMessageType.isEmpty()) {
			logger.error("Event code not found in the MessageHeader for validation.");
			throw new EicrException("Event code missing in the MessageHeader.");
		}

		ValidationEngine validationEngine = validationEngineFactory.getValidationEngine(validatorMessageType);

		try {

			logger.debug("Fhir Validation Starts: {}", profiles);
			byte[] resourceDataBytes = resourceData.getBytes();
			Manager.FhirFormat fhirFormat = FormatUtilities.determineFormat(resourceDataBytes);
			List<ValidationMessage> messages = new ArrayList<>();
			// Long startTime = System.currentTimeMillis();
			OperationOutcome operationOutcome = validationEngine.validate(resourceDataBytes, fhirFormat,
					resourceProfiles, messages);
			/*
			 * Long startTime = System.currentTimeMillis(); logger.
			 * debug("{} Ends Level2 Fhir server validation Time Taken in milliseconds: {}",
			 * profiles,(System.currentTimeMillis()-startTime)/1000);
			 */
			logger.debug("Messages: {}", messages.size());
			logger.debug("operationOutcome validate: {}", operationOutcome);
			messages.forEach(message -> {
				logger.debug(message.getMessage());
			});
			return Optional.ofNullable(operationOutcome);
		} catch (Exception e) {
			logger.debug("Fhir Validation Failed: {}:{}", profiles, e.getMessage(), e);
			if (Objects.isNull(validationEngine)) {
				logger.debug("{}: {}", e.getClass().getName(), e.getMessage());
				throw new EicrException("Validation Engine is not initialized", e);
			} else {
				logger.debug("{}: {}", e.getClass().getName(), e.getMessage());
				throw new EicrException(e.getMessage(), e);
			}
		}
	}

}
