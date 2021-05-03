package org.sitenv.spring.dao;

import java.util.List;

import org.sitenv.spring.model.DafValueSet;

public interface ValueSetDao {

	DafValueSet getValueSetById(String paramString);
	  
	  List<DafValueSet> getAllValueSets();
	  
	  void createValueSet(DafValueSet paramDafValueSet);
}
