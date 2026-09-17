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
        getSession().delete(entity);
    }


}
