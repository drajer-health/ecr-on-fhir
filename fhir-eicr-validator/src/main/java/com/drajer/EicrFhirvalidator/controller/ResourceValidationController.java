package com.drajer.EicrFhirvalidator.controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import com.drajer.EicrFhirvalidator.service.ResourceValidationService;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResourceValidationController {
	private static final Logger logger = LoggerFactory.getLogger(ResourceValidationController.class);

	@Autowired
	@Qualifier("r4FhirContext")
	private FhirContext r4Context;

	@Autowired
	@Qualifier("r5FhirContext")
	private FhirContext r5Context;

	@Autowired
	private ResourceValidationService validationService;

	/**
	 * Validates r4 resources
	 * 
	 * @param bodyStr
	 * @return
	 */
	@PostMapping(value = "/r4/resource/validate")
	public ResponseEntity<?> validateR4Resource(@RequestBody String bodyStr,
			@OptionalParam(name = "profile") String profile) throws Exception {
		String output = null;
		FhirValidator validator = r4Context.newValidator();

			if (profile != null) {
				try {
					String results;
					logger.info("Validating with Profile : "+ profile);
					OperationOutcome oo = validationService.validate(bodyStr.getBytes(), profile);
					results = r5Context.newJsonParser().setPrettyPrint(true).encodeResourceToString(oo);
					return new ResponseEntity<>(results, HttpStatus.OK);
				} catch (Exception e) {
					logger.error("Exception in validateR4Resource");
					org.hl7.fhir.r4.model.OperationOutcome outcomes = new org.hl7.fhir.r4.model.OperationOutcome();
					outcomes.addIssue().setSeverity(org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR)
							.setDiagnostics("Failed to parse request body as JSON resource. Error was: " + e.getMessage());
					output = r4Context.newJsonParser().setPrettyPrint(true).encodeResourceToString(outcomes);
				}
			} else {
				Resource resource = (Resource) r4Context.newJsonParser().setPrettyPrint(true).parseResource(bodyStr);

				String resProfile =null;
				if(resource.hasMeta()) {
					Meta resMeta = resource.getMeta();
					if(resMeta.hasProfile()) {
						CanonicalType canonicalProfileType =resMeta.getProfile().get(0); 
						resProfile = canonicalProfileType.asStringValue();
					}
				}
				if(resProfile != null) {
					try {
						String results;
						logger.info("Validating with Profile====> "+resProfile);
						OperationOutcome oo = validationService.validate(bodyStr.getBytes(), resProfile);
						results = r5Context.newJsonParser().setPrettyPrint(true).encodeResourceToString(oo);
						return new ResponseEntity<>(results, HttpStatus.OK);
					} catch (Exception e) {
						logger.error("Exception in validateR4Resource");
						org.hl7.fhir.r4.model.OperationOutcome outcomes = new org.hl7.fhir.r4.model.OperationOutcome();
						outcomes.addIssue().setSeverity(org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR)
								.setDiagnostics("Failed to parse request body as JSON resource. Error was: " + e.getMessage());
						output = r4Context.newJsonParser().setPrettyPrint(true).encodeResourceToString(outcomes);
					}
				} else {
					try {
						ValidationResult results = validationService.validateR4Resource(r4Context, validator, bodyStr);
						if (results instanceof ValidationResult && results.isSuccessful()) {
							logger.info("Validation passed");
						} else {
							logger.error("Failed to validateR4Resource.");
						}
						org.hl7.fhir.r4.model.OperationOutcome oo = (org.hl7.fhir.r4.model.OperationOutcome) results
								.toOperationOutcome();
						output = r4Context.newJsonParser().setPrettyPrint(true).encodeResourceToString(oo);
						return new ResponseEntity<>(output, HttpStatus.OK);
					} catch (DataFormatException e) {
						logger.error("Exception in validateR4Resource");
						org.hl7.fhir.r4.model.OperationOutcome outcomes = new org.hl7.fhir.r4.model.OperationOutcome();
						outcomes.addIssue().setSeverity(org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR)
								.setDiagnostics("Failed to parse request body as JSON resource. Error was: " + e.getMessage());
						output = r4Context.newJsonParser().setPrettyPrint(true).encodeResourceToString(outcomes);
					}	
				}
			}
			logger.info("Before Return");
			return new ResponseEntity<>(output, HttpStatus.INTERNAL_SERVER_ERROR);
		}

}