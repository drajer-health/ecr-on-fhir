package org.sitenv.spring.service;

import java.util.List;

import org.sitenv.spring.dao.PlanDefinitionDao;
import org.sitenv.spring.dao.ValueSetDao;
import org.sitenv.spring.model.DafValueSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("ValueSetService")
@Transactional
public class ValueSetServiceImpl implements ValueSetService{
	
	@Autowired
	  private ValueSetDao valueSetDao;
	  
	  public DafValueSet getValueSetById(String id) {
	    return this.valueSetDao.getValueSetById(id);
	  }
	  
	  public List<DafValueSet> getAllValueSets() {
	    return this.valueSetDao.getAllValueSets();
	  }
	  
	  public void createValueSets(DafValueSet dafValueSet) {
	    this.valueSetDao.createValueSet(dafValueSet);
	  }
}
