package org.sitenv.spring.service;

import org.sitenv.spring.model.DafCommunication;

/**
 * Service contract for {@link DafCommunication} records.
 */
public interface CommunicationService {

	/**
	 * @param paramString the FHIR logical id of the Communication resource
	 * @return the matching communication record (highest version first)
	 */
	DafCommunication getPlanDefinitionById(String paramString);

	/**
	 * Saves or updates a communication record.
	 *
	 * @param paramDafCommunication the communication to persist
	 */
	void createCommunication(DafCommunication paramDafCommunication);

}
