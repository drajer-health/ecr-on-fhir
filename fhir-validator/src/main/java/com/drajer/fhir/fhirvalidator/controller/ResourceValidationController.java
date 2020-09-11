package com.drajer.fhir.fhirvalidator.controller;

import org.hl7.fhir.r5.model.OperationOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.drajer.fhir.fhirvalidator.service.ResourceValidationService;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;

@RestController
public class ResourceValidationController {
	private static final Logger logger = LoggerFactory.getLogger(ResourceValidationController.class);
//	private static ResourceValidationController resourceValidationController = null;

	@Autowired
	@Qualifier("r4FhirContext")
	private FhirContext r4Context;

	@Autowired
	@Qualifier("dstu3FhirContext")
	private FhirContext dstu3Context;

	@Autowired
	@Qualifier("dstu2FhirContext")
	private FhirContext dstu2Context;

	@Autowired
	@Qualifier("dstu2HL7FhirContext")
	private FhirContext dstu2HL7FhirContext;

	@Autowired
	@Qualifier("r5FhirContext")
	private FhirContext r5Context;

	@Autowired
	private ResourceValidationService validationService;


	@PostMapping(value = "/dstu2/resource/validate")
	public ResponseEntity<?> validateDSTU2Resource(@RequestBody String bodyStr) {
		String output = null;
		FhirValidator validator = dstu2HL7FhirContext.newValidator();
		try {
			ValidationResult results = validationService.validateDSTU2Resource(dstu2HL7FhirContext, validator, bodyStr);
			if (results instanceof ValidationResult && results.isSuccessful()) {
				logger.info("Validation passed");
				output = dstu2operationOutcome(results);
				return new ResponseEntity<>(output, HttpStatus.OK);
			} else {
				logger.error("Failed to validateDSTU2Resource.");
				output = dstu2operationOutcome(results);
				return new ResponseEntity<>(output, HttpStatus.BAD_REQUEST);
			}

		} catch (DataFormatException e) {
			logger.error("Exception in validateDSTU2Resource.");
			org.hl7.fhir.dstu2.model.OperationOutcome outcomes = new org.hl7.fhir.dstu2.model.OperationOutcome();
			outcomes.addIssue().setSeverity(org.hl7.fhir.dstu2.model.OperationOutcome.IssueSeverity.ERROR)
					.setDiagnostics("Failed to parse request body as JSON resource. Error was: " + e.getMessage());
			output = dstu2HL7FhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(outcomes);
		}
		return new ResponseEntity<>(output, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * Validates STU3 resources
	 * 
	 * @param bodyStr
	 * @return
	 */
	@PostMapping(value = "/stu3/resource/validate")
	public ResponseEntity<?> validateSTU3Resource(@RequestBody String bodyStr) {
		String output = null;
		FhirValidator validator = dstu3Context.newValidator();
		try {
			ValidationResult results = validationService.validateSTU3Resource(dstu3Context, validator, bodyStr);
			if (results instanceof ValidationResult && results.isSuccessful()) {
				logger.info("Validation passed");
				output = dstu3operationOutcome(results);
				return new ResponseEntity<>(output, HttpStatus.OK);
			} else {
				logger.error("Failed to validateSTU3Resource.");
				output = dstu3operationOutcome(results);
				return new ResponseEntity<>(output, HttpStatus.BAD_REQUEST);
			}

		} catch (DataFormatException e) {
			logger.error("Exception in validateSTU3Resource");
			org.hl7.fhir.dstu3.model.OperationOutcome outcomes = new org.hl7.fhir.dstu3.model.OperationOutcome();
			outcomes.addIssue().setSeverity(org.hl7.fhir.dstu3.model.OperationOutcome.IssueSeverity.ERROR)
					.setDiagnostics("Failed to parse request body as JSON resource. Error was: " + e.getMessage());
			output = dstu3Context.newJsonParser().setPrettyPrint(true).encodeResourceToString(outcomes);
		}
		return new ResponseEntity<>(output, HttpStatus.INTERNAL_SERVER_ERROR);

	}

	/**
	 * Validates r4 resources
	 * 
	 * @param bodyStr
	 * @return
	 */
	@PostMapping(value = "/r4/{encoding}/validate")
	public ResponseEntity<?> validateR4Resource(@PathVariable("encoding") String encoding, @RequestBody String bodyStr,
												@OptionalParam(name = "profile") String profile) throws Exception {
		String output = null;
		FhirValidator validator = r4Context.newValidator();
			if (profile != null) {
				try {
					String results;
					logger.info("validating us-core-R4Resource");
					OperationOutcome oo = validationService.validate(bodyStr.getBytes(), profile);
					results = r5Context.newJsonParser().setPrettyPrint(true).encodeResourceToString(oo);
					return new ResponseEntity<>(results, HttpStatus.OK);
				} catch (NullPointerException e) {
					logger.error("Exception in validateR4Resource");
					e.printStackTrace();
					return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				try {
					ValidationResult results = validationService.validateR4Resource(r4Context, validator, bodyStr, encoding);
					if (results instanceof ValidationResult && results.isSuccessful()) {
						logger.info("Validation passed");
						org.hl7.fhir.r4.model.OperationOutcome oo = (org.hl7.fhir.r4.model.OperationOutcome) results
								.toOperationOutcome();
						if("json".equalsIgnoreCase(encoding))
							output = r4Context.newJsonParser().setPrettyPrint(true).encodeResourceToString(oo);
						else
							output = r4Context.newXmlParser().setPrettyPrint(true).encodeResourceToString(oo);

						return new ResponseEntity<>(output, HttpStatus.OK);
					} else {
						logger.error("Failed to validateR4Resource.");
						org.hl7.fhir.r4.model.OperationOutcome oo = (org.hl7.fhir.r4.model.OperationOutcome) results
								.toOperationOutcome();
						if("json".equalsIgnoreCase(encoding))
							output = r4Context.newJsonParser().setPrettyPrint(true).encodeResourceToString(oo);
						else
							output = r4Context.newXmlParser().setPrettyPrint(true).encodeResourceToString(oo);

						return new ResponseEntity<>(output, HttpStatus.OK);
					}
				} catch (DataFormatException e) {
					logger.error("Exception in validateR4Resource");
					org.hl7.fhir.r4.model.OperationOutcome outcomes = new org.hl7.fhir.r4.model.OperationOutcome();
					outcomes.addIssue().setSeverity(org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR)
							.setDiagnostics("Failed to parse request body as JSON resource. Error was: " + e.getMessage());

					if("json".equalsIgnoreCase(encoding))
						output = r4Context.newJsonParser().setPrettyPrint(true).encodeResourceToString(outcomes);
					else
						output = r4Context.newXmlParser().setPrettyPrint(true).encodeResourceToString(outcomes);

				}
			}
			return new ResponseEntity<>(output, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	/*
	 * gives dstu2 operationOutcome type result
	 */
	private String dstu2operationOutcome(ValidationResult results) {
		org.hl7.fhir.dstu2.model.OperationOutcome oo = (org.hl7.fhir.dstu2.model.OperationOutcome) results
				.toOperationOutcome();
		return dstu2HL7FhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(oo);
	}
	
	/*
	 * gives dstu3 operationOutcome type result
	 */
	private String dstu3operationOutcome(ValidationResult results) {
		org.hl7.fhir.dstu3.model.OperationOutcome oo = (org.hl7.fhir.dstu3.model.OperationOutcome) results
				.toOperationOutcome();
		return dstu3Context.newJsonParser().setPrettyPrint(true).encodeResourceToString(oo);
	}
}