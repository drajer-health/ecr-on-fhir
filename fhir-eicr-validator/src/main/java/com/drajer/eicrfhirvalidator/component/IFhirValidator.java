
package com.drajer.eicrfhirvalidator.component;



import com.drajer.eicrfhirvalidator.exception.EicrException;

import java.util.Optional;

/**
 * Abstraction over a FHIR resource validator, decoupling callers from the specific validation
 * engine implementation (e.g. the embedded HL7 {@code ValidationEngine}).
 *
 * @param <I> the input resource representation type (e.g. a JSON string)
 * @param <O> the validation outcome type (e.g. {@code OperationOutcome})
 * @author Drajer LLC
 * @since 23-03-2023
 */
public interface IFhirValidator<I, O> {

  /**
   * Validating an resources, by its json data and profile
   *
   * @param resourceData String json data
   * @param profile String profile url of the resource
   * @return Optional String
   * @throws Exception, when validation failed
   */
  Optional<O> validate(I resourceData, String profile) throws EicrException;
}
