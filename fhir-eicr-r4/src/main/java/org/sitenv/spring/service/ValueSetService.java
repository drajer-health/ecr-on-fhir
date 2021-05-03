package org.sitenv.spring.service;

import java.util.List;

import org.sitenv.spring.model.DafValueSet;

public interface ValueSetService {
	
	DafValueSet getValueSetById(String paramString);
	
	List<DafValueSet> getAllValueSets();
	  
	  void createValueSets(DafValueSet paramDafValueSet);

}
