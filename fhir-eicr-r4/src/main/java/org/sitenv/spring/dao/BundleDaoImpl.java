package org.sitenv.spring.dao;

import org.sitenv.spring.model.DafBundle;
import org.springframework.stereotype.Repository;

/**
 * Hibernate-backed implementation of {@link BundleDao}.
 */
@Repository("BundleDao")
public class BundleDaoImpl extends AbstractDao implements BundleDao{

	public void createBundle(DafBundle dafBundle) {
	    getSession().saveOrUpdate(dafBundle);
	  }
}
