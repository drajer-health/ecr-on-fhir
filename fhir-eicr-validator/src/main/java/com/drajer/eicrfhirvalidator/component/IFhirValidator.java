
package com.drajer.eicrfhirvalidator.component;


import com.drajer.eicrfhirvalidator.exception.EicrException;

import java.util.Optional;

/**
 * Interface IFhirValidator TODO
 *
 * @param <I>
 * @param <O>
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
