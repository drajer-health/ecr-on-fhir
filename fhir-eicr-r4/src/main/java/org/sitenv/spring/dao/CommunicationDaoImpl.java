package org.sitenv.spring.dao;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.sitenv.spring.model.DafCommunication;
import org.springframework.stereotype.Repository;

@Repository("CommunicationDao")
public class CommunicationDaoImpl extends AbstractDao implements CommunicationDao{

	public DafCommunication getCommunicationById(String id) {
		Criteria criteria = getSession().createCriteria(DafCommunication.class).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	    criteria.add(Restrictions.sqlRestriction("{alias}.data->>'id' = '" + id + "' order by {alias}.data->'meta'->>'versionId' desc"));
	    return (DafCommunication) criteria.list().get(0);
	}

	public void createCommunication(DafCommunication dafCommunication) {
		getSession().saveOrUpdate(dafCommunication);
	}

}
