package org.sitenv.spring.dao;

import org.sitenv.spring.model.DafCommunication;

/**
 * Persistence contract for {@link DafCommunication} records.
 */
public interface CommunicationDao {

	/**
	 * @param paramString the FHIR logical id of the Communication resource
	 * @return the matching communication record (highest version first)
	 */
	DafCommunication getCommunicationById(String paramString);

	/**
	 * Saves or updates a communication record.
	 *
	 * @param dafCommunication the communication to persist
	 */
	void createCommunication(DafCommunication dafCommunication);
}
