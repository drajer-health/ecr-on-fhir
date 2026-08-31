package com.drajer.eicrfhirvalidator.component;


import com.drajer.eicrfhirvalidator.exception.EicrException;
import org.hl7.fhir.r5.elementmodel.Manager;
import org.hl7.fhir.r5.formats.FormatUtilities;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.hl7.fhir.validation.ValidationEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import java.util.*;


/**
 * {@link IFhirValidator} implementation backed by an HL7 {@link ValidationEngine}, validating
 * a resource (as a JSON or XML string, auto-detected via {@link FormatUtilities#determineFormat})
 * against one or more implementation guide profiles.
 *
 * @author Drajer LLC
 * @since 27-04-2023
 */
@ConditionalOnClass(ValidationEngine.class)
public class EmbeddedHapiFhirValidator implements IFhirValidator<String, OperationOutcome> {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedHapiFhirValidator.class);
    private static final String COMMA_SEPARATOR = ",";

    private final ValidationEngine validationEngine;

    /**
     * @param validationEngine the underlying HL7 validation engine, prepared with the
     *     implementation guides to validate against
     */
    public EmbeddedHapiFhirValidator(ValidationEngine validationEngine) {
        this.validationEngine = validationEngine;
    }

    /**
     * {@inheritDoc}
     *
     * @throws EicrException if {@link #validationEngine} is not initialized, or validation
     *     otherwise fails
     */
    @Override
    public Optional<OperationOutcome> validate(String resourceData, String profiles) throws EicrException {
        List<String> resourceProfiles = profiles.contains(COMMA_SEPARATOR)
                ? Arrays.asList(profiles.split(COMMA_SEPARATOR))
                : List.of(profiles);
        try {
            logger.debug("Fhir Validation Starts: {}", profiles);
            byte[] resourceDataBytes = resourceData.getBytes();
            Manager.FhirFormat fhirFormat = FormatUtilities.determineFormat(resourceDataBytes);
            List<ValidationMessage> messages = new ArrayList<>();
            //Long startTime = System.currentTimeMillis();
            OperationOutcome operationOutcome = validationEngine.validate(resourceDataBytes, fhirFormat, resourceProfiles, messages);
            /* Long startTime = System.currentTimeMillis();
            logger.debug("{} Ends Level2 Fhir server validation Time Taken in milliseconds: {}", profiles,(System.currentTimeMillis()-startTime)/1000);*/
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
