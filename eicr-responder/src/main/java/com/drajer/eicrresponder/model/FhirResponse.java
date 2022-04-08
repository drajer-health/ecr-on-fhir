package com.drajer.eicrresponder.model;

import org.hl7.fhir.r4.model.Bundle;

/**
 * @author Girish Rao 
 * Fhir response object
 */
public class FhirResponse {

	Bundle submitResponse;

	/**
	 * @return submitResponse
	 */
	public Bundle getSubmitResponse() {
		return submitResponse;
	}

	/**
	 * @param submitResponse
	 */
	public void setSubmitResponse(Bundle submitResponse) {
		this.submitResponse = submitResponse;
	}

	@Override
	public String toString() {
		return "FhirResponse [submitResponse=" + submitResponse + "]";
	}

}
