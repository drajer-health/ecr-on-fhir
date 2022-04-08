package com.drajer.eicrresponder.service.Interface;

import org.springframework.http.ResponseEntity;

import com.drajer.eicrresponder.model.FhirRequest;
import com.drajer.eicrresponder.model.ResponderRequest;

/**
 * @author Girish Rao
 *
 */
public interface FhirService {
	ResponseEntity<String> submitToFhir(FhirRequest fhirResquest, ResponderRequest responderRequest);
}
