package org.sitenv.spring.dao;

import org.hibernate.query.NativeQuery;
import org.sitenv.spring.model.DafCommunication;
import org.springframework.stereotype.Repository;

/**
 * Hibernate-backed implementation of {@link CommunicationDao}, querying the
 * JSONB {@code data} column of the {@code communication} table directly via
 * native SQL.
 */
@Repository("CommunicationDao")
public class CommunicationDaoImpl extends AbstractDao implements CommunicationDao{

	public DafCommunication getCommunicationById(String id) {
		NativeQuery<DafCommunication> query = getSession().createNativeQuery(
				"select * from communication where data->>'id' = :id order by data->'meta'->>'versionId' desc",
				DafCommunication.class);
		query.setParameter("id", id);
		return query.list().get(0);
	}

	public void createCommunication(DafCommunication dafCommunication) {
		getSession().saveOrUpdate(dafCommunication);
	}

}
