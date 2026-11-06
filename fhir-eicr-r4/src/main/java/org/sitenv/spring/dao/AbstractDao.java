package org.sitenv.spring.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for Hibernate-backed DAOs, providing access to the current
 * transactional {@link Session} plus generic persist/delete helpers.
 */
public abstract class AbstractDao {

    @Autowired
    private SessionFactory sessionFactory;

    /**
     * @return the Hibernate session bound to the current transaction
     */
    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    /**
     * Persists a new entity.
     *
     * @param entity the entity to save
     */
    public void persist(Object entity) {
        getSession().persist(entity);
    }

    /**
     * Deletes an existing entity.
     *
     * @param entity the entity to delete
     */
    public void delete(Object entity) {
        // Hibernate 7 removed Session.delete(); remove() requires a managed instance.
        Session session = getSession();
        session.remove(session.contains(entity) ? entity : session.merge(entity));
    }

    /**
     * Inserts a new entity or updates an existing one. Replaces the
     * {@code Session.saveOrUpdate()} call removed in Hibernate 7: transient entities
     * (no identifier yet) are persisted so the caller's instance receives the
     * generated id; detached entities are merged.
     *
     * @param entity the entity to save or update
     */
    public void saveOrUpdate(Object entity) {
        Session session = getSession();
        if (sessionFactory.getPersistenceUnitUtil().getIdentifier(entity) == null) {
            session.persist(entity);
        } else {
            session.merge(entity);
        }
    }


}
