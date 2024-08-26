
package com.drajer.eicrfhirvalidator.exception;

/**
 * Class FhirServerNotAvailableException TODO
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
