package com.drajer.eicrfhirvalidator.configuration;

import org.hl7.fhir.validation.ValidationEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.drajer.eicrfhirvalidator.component.Validator;
import java.util.HashMap;
import java.util.Map;

@Component
public class ValidationEngineFactory {

	private static final Logger logger = LoggerFactory.getLogger(ValidationEngineFactory.class);

	private final Map<String, ValidationEngine> engineCache = new HashMap<>();
	private final Validator validator;

	public ValidationEngineFactory(Validator validator) {
		this.validator = validator;
	}

	public ValidationEngine getValidationEngine(String messageTypeSettings) {
		logger.info("Request to load ValidationEngine for message type: {}", messageTypeSettings);

		// Check if the ValidationEngine for the given messageTypeSettings is already
		// cached
		if (engineCache.containsKey(messageTypeSettings)) {
			logger.info("ValidationEngine for message type '{}' loaded from cache", messageTypeSettings);
			return engineCache.get(messageTypeSettings);
		}

		logger.info("ValidationEngine for message type '{}' not found in cache. Creating new engine.",
				messageTypeSettings);

		String igPathBySetting = validator.getIgPathBySetting(messageTypeSettings);
		if (igPathBySetting != null) {
			ValidationEngine newEngine = validator.createValidationEngine(messageTypeSettings);
			if (newEngine != null) {
				engineCache.put(messageTypeSettings, newEngine);
				logger.info("New ValidationEngine created and cached for message type: {}", messageTypeSettings);
				return newEngine;
			} else {
				logger.error("Failed to create ValidationEngine for message type: {}", messageTypeSettings);
				throw new RuntimeException(
						"Failed to create ValidationEngine for message type: " + messageTypeSettings);
			}
		} else {
			logger.error("Invalid message type settings for validator: {}", messageTypeSettings);
			throw new IllegalArgumentException("Invalid message type settings for validator: " + messageTypeSettings);
		}
	}

}
