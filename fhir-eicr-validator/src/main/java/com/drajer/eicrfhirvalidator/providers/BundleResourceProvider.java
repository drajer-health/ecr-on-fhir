package com.drajer.eicrfhirvalidator.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import com.drajer.eicrfhirvalidator.service.ResourceValidationService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class BundleResourceProvider implements IResourceProvider {
    private static final Logger logger = LoggerFactory.getLogger(BundleResourceProvider.class);

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return  Bundle.class;
    }

    @Autowired
    @Qualifier("r4FhirContext")
    private FhirContext r4Context;

    @Autowired
    @Qualifier("r5FhirContext")
    private FhirContext r5Context;

    @Autowired
    ResourceValidationService validationService;

    @Operation(name = "$validate", idempotent = true)
    public OperationOutcome validateResource(
            @OperationParam(name = "resource") Bundle bundle,
            @OperationParam(name = "profile") String profile,
            RequestDetails requestDetails) {

        FhirValidator validator = r4Context.newValidator();
        String bundleAsJsonString = r4Context.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);

        if(profile == null){
            if(bundle.hasMeta()) {
                Meta resMeta = bundle.getMeta();
                if(resMeta.hasProfile()) {
                    CanonicalType canonicalProfileType =resMeta.getProfile().get(0);
                    profile = canonicalProfileType.asStringValue();
                }
            }
        }

        if(profile != null){
            try {
                String results;
                logger.info("Validating with Profile : "+ profile);
                OperationOutcome outcomes = validationService.validate(bundleAsJsonString, profile);
                return outcomes;

            } catch (Exception e) {
                OperationOutcome outcomes = new OperationOutcome();
                outcomes.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR)
                        .setDiagnostics("Failed to parse request body as JSON resource. Error was: " + e.getMessage());
                return outcomes;
            }
        }else{
            try {
                ValidationResult results = validationService.validateR4Resource(r4Context, validator, bundleAsJsonString);
                if (results instanceof ValidationResult && results.isSuccessful()) {
                    logger.info("Validation passed");
                } else {
                    logger.error("Failed to validateR4Resource.");
                }
                OperationOutcome outcomes = (OperationOutcome) results.toOperationOutcome();
                return outcomes;
            } catch (DataFormatException e) {
                logger.error("Exception in validateR4Resource");
                OperationOutcome outcomes = new OperationOutcome();
                outcomes.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR)
                        .setDiagnostics("Failed to parse request body as JSON resource. Error was: " + e.getMessage());
                return outcomes;
            }
        }
    }
}
