package org.sitenv.spring.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.sitenv.spring.model.DafValueSet;
import org.springframework.stereotype.Repository;

@Repository("ValueSetDao")
public class ValueSetDaoImpl extends AbstractDao implements ValueSetDao{

	public DafValueSet getValueSetById(String id) {
	    Criteria criteria = getSession().createCriteria(DafValueSet.class).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	    criteria.add(Restrictions.sqlRestriction("{alias}.data->>'id' = '" + id + "' order by {alias}.data->'meta'->>'versionId' desc"));
	    return (DafValueSet) criteria.list().get(0);
	  }
	  
	  public List<DafValueSet> getAllValueSets() {
	    Criteria criteria = getSession().createCriteria(DafValueSet.class).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	    criteria.add(Restrictions.sqlRestriction("{alias}.data->'meta'->>'versionId' desc"));
	    return criteria.list();
	  }
	  
	  public void createValueSet(DafValueSet dafValueSet) {
	    getSession().saveOrUpdate(dafValueSet);
	  }
}
