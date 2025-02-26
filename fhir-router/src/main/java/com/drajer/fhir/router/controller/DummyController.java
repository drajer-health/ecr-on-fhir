package com.drajer.fhir.router.controller;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DummyController {

    @GetMapping("/fhirerror")
    public ResponseEntity<OperationOutcome> returnError() {
        OperationOutcome outcome = new OperationOutcome();
        outcome.addIssue()
            .setSeverity(IssueSeverity.ERROR)
            .setCode(IssueType.EXCEPTION)
            .setDetails(new CodeableConcept().addCoding(
                new Coding().setSystem("http://terminology.hl7.org/CodeSystem/operation-outcome")
                    .setCode("exception")
                    .setDisplay("Exception")))
            .setDiagnostics("An unexpected error occurred.");

        return new ResponseEntity<>(outcome, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @PostMapping("/fhirsuccess/$process-message")
    public ResponseEntity<OperationOutcome> returnSuccess() {
    	System.out.println("success fhir OperationOutcome !!!!!$$$$$$$$");
         OperationOutcome outcome = new OperationOutcome();
        outcome.addIssue()
            .setSeverity(IssueSeverity.INFORMATION)
            .setCode(IssueType.INFORMATIONAL)
            .setDetails(new CodeableConcept().addCoding(
                new Coding().setSystem("http://terminology.hl7.org/CodeSystem/operation-outcome")
                    .setCode("informational")
                    .setDisplay("Informational")))
            .setDiagnostics("The operation was successful.");
        return new ResponseEntity<>(outcome, HttpStatus.OK);
    }    
}
