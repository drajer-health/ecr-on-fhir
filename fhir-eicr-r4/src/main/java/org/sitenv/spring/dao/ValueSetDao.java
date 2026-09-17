package org.sitenv.spring.dao;

import java.util.List;

import org.sitenv.spring.model.DafValueSet;

/**
 * Persistence contract for {@link DafValueSet} records.
 */
public interface ValueSetDao {

	/**
	 * @param paramString the FHIR logical id of the ValueSet resource
	 * @return the matching value set record (highest version first)
	 */
	DafValueSet getValueSetById(String paramString);

	  /**
	   * @return all value set records, most recently versioned first
	   */
	  List<DafValueSet> getAllValueSets();

	  /**
	   * Saves or updates a value set record.
	   *
	   * @param paramDafValueSet the value set to persist
	   */
	  void createValueSet(DafValueSet paramDafValueSet);
}
