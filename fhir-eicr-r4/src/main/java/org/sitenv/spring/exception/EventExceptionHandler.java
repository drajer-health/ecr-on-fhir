package org.sitenv.spring.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Global MVC exception handler translating {@link FHIRHapiException} into a
 * uniform HTTP 500 response.
 */
@ControllerAdvice
public class EventExceptionHandler extends ResponseEntityExceptionHandler {

    public EventExceptionHandler() {
        super();
    }

    /**
     * @param de the thrown FHIR/HAPI exception
     * @param request the current web request
     * @return an HTTP 500 response with a generic client-facing message
     */
    @ExceptionHandler({FHIRHapiException.class})
    public ResponseEntity<Object> handleExceptionInternal(final FHIRHapiException de, final WebRequest request) {
        final String bodyOfResponse = "Client Name is already existed. Please use a different Client Name";
        return handleExceptionInternal(de, bodyOfResponse, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

}
