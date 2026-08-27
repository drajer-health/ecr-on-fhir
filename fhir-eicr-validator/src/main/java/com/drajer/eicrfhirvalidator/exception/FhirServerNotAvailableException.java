
package com.drajer.eicrfhirvalidator.exception;

/**
 * Thrown when the FHIR validation engine has not been configured/initialized and a validation
 * request cannot be serviced.
 *
 * @author Drajer LLC
 * @since 25-08-2023
 */
public class FhirServerNotAvailableException extends EicrException {
  public FhirServerNotAvailableException() {
  }

  public FhirServerNotAvailableException(String message) {
    super(message);
  }

  public FhirServerNotAvailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
