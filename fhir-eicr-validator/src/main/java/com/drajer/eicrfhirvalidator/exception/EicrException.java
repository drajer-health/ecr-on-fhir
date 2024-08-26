
package com.drajer.eicrfhirvalidator.exception;

/**
 * Class EicrException TODO
 *
 * @author Drajer LLC
 * @since 23-03-2023
 */
public class EicrException extends RuntimeException {

  public EicrException() {
  }

  public EicrException(String message) {
    super(message);
  }

  public EicrException(String message, Throwable cause) {
    super(message, cause);
  }
}
