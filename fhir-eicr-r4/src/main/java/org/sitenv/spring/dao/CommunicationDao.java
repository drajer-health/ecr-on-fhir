package org.sitenv.spring.dao;

import org.sitenv.spring.model.DafCommunication;

public interface CommunicationDao {

	DafCommunication getCommunicationById(String paramString);
	
	void createCommunication(DafCommunication dafCommunication);
}
