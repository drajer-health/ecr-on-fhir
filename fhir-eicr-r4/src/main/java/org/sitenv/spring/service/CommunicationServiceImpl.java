package org.sitenv.spring.service;

import org.sitenv.spring.dao.CommunicationDao;
import org.sitenv.spring.model.DafCommunication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("CommunicationService")
@Transactional
public class CommunicationServiceImpl implements CommunicationService{

	@Autowired
	  private CommunicationDao communicationDao;

	@Override
	public DafCommunication getPlanDefinitionById(String paramString) {
		return this.communicationDao.getCommunicationById(paramString);
	}

	@Override
	public void createCommunication(DafCommunication paramDafCommunication) {
		this.communicationDao.createCommunication(paramDafCommunication);
		
	}
}
