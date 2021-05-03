package org.sitenv.spring.service;

import org.sitenv.spring.model.DafCommunication;

public interface CommunicationService {
	
	DafCommunication getPlanDefinitionById(String paramString);
	
	void createCommunication(DafCommunication paramDafCommunication);

}
