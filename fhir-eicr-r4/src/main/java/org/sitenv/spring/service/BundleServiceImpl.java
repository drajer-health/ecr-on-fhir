package org.sitenv.spring.service;

import org.sitenv.spring.dao.BundleDao;
import org.sitenv.spring.model.DafBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("BundleService")
@Transactional
public class BundleServiceImpl implements BundleService{

	@Autowired
	  private BundleDao bundleDao;
	
	public void createBundle(DafBundle dafBundle) {
	    this.bundleDao.createBundle(dafBundle);
	  }
}
