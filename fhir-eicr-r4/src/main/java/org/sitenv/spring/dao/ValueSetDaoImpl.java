package org.sitenv.spring.dao;

import java.util.List;

import org.hibernate.query.NativeQuery;
import org.sitenv.spring.model.DafValueSet;
import org.springframework.stereotype.Repository;

/**
 * Hibernate-backed implementation of {@link ValueSetDao}, querying the JSONB
 * {@code data} column of the {@code valueset} table directly via native SQL.
 */
@Repository("ValueSetDao")
public class ValueSetDaoImpl extends AbstractDao implements ValueSetDao{

	public DafValueSet getValueSetById(String id) {
		NativeQuery<DafValueSet> query = getSession().createNativeQuery(
				"select * from valueset where data->>'id' = :id order by data->'meta'->>'versionId' desc",
				DafValueSet.class);
		query.setParameter("id", id);
		return query.list().get(0);
	}

	public List<DafValueSet> getAllValueSets() {
		NativeQuery<DafValueSet> query = getSession().createNativeQuery(
				"select * from valueset order by data->'meta'->>'versionId' desc",
				DafValueSet.class);
		return query.list();
	}

	public void createValueSet(DafValueSet dafValueSet) {
		getSession().saveOrUpdate(dafValueSet);
	}
}
