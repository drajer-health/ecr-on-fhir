package org.sitenv.spring.controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.LenientErrorHandler;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.ValidationResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.hapi.validation.FhirInstanceValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ValidateRestController {

    /**
     * @param encoding -- Specify the input encoding Ex Values: json , xml
     * @param bodyStr -- Pass resource data as part of body string
     * @return Retruns OperationOutcome formatted validation results
     */

    @PostMapping(value = "/r4/{encoding}/validate")
    public ResponseEntity validateR4(@PathVariable("encoding") String encoding, @RequestBody String bodyStr) {

        String results;
        IBaseResource resource = null;

        FhirContext ctx = FhirContext.forR4();
//      Ask the context for a validator
        FhirValidator validator = ctx.newValidator();

        IValidatorModule module = new FhirInstanceValidator();
        validator.registerValidatorModule(module);

        try{
            if("xml".equalsIgnoreCase(encoding)) {
                // Parse that XML string encoding
                resource = ctx.newXmlParser().setParserErrorHandler(new LenientErrorHandler().setErrorOnInvalidValue(false)).parseResource(bodyStr);
            }else{
                // Parse that JSON string encoding)
                resource = ctx.newJsonParser().setParserErrorHandler(new LenientErrorHandler().setErrorOnInvalidValue(false)).parseResource(bodyStr);
            }

            // Apply the validation. This will throw an exception on the first validation failure
            ValidationResult result = validator.validateWithResult(resource);
            //For JSON output
            results = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(result.toOperationOutcome());

            // For XML output
            //results = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(result.toOperationOutcome()); // XML output

        } catch (DataFormatException e) {
            e.printStackTrace();
            results = "";
        }
//      The result object now contains the validation results
        return new ResponseEntity(results, HttpStatus.OK);
    }

    /**
     * @param encoding -- Specify the input encoding Ex Values: json , xml
     * @param bodyStr -- Pass resource data as part of body string
     * @return JSON formatted validation results
     */

    @PostMapping(value = "/ecr/{encoding}/validate")
    public ResponseEntity validateECR(@PathVariable("encoding") String encoding, @RequestBody String bodyStr) {

        String results ="";
        IBaseResource resource = null;
        FhirContext ctx = FhirContext.forR4();

//      Create a validation support chain
        FhirValidator validator = ctx.newValidator();
        validator.setValidateAgainstStandardSchema(true);

        FhirInstanceValidator instanceValidator = new FhirInstanceValidator();
        instanceValidator.setAnyExtensionsAllowed(true);
        validator.registerValidatorModule(instanceValidator);

        try{
            if("xml".equalsIgnoreCase(encoding)) {
                // Parse that XML string encoding
                resource = ctx.newXmlParser().setParserErrorHandler(new LenientErrorHandler().setErrorOnInvalidValue(false)).parseResource(bodyStr);
            }else{
                // Parse that JSON string encoding)
                resource = ctx.newJsonParser().setParserErrorHandler(new LenientErrorHandler().setErrorOnInvalidValue(false)).parseResource(bodyStr);
            }

            // Apply the validation. This will throw an exception on the first validation failure
            ValidationResult result = validator.validateWithResult(resource);
            //For JSON output
            results = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(result.toOperationOutcome());

            // For XML output
            //results = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(result.toOperationOutcome()); // XML output

        } catch (DataFormatException e) {
            results = e.getMessage();
            return new ResponseEntity(results, HttpStatus.EXPECTATION_FAILED);
        }
// The result object now contains the validation results
        return new ResponseEntity(results, HttpStatus.OK);
    }


}