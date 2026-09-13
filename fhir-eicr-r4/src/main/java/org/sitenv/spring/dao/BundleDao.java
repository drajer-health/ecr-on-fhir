package org.sitenv.spring.dao;

import org.sitenv.spring.model.DafBundle;

/**
 * Persistence contract for eICR {@link DafBundle} records.
 */
public interface BundleDao {

	/**
	 * Saves or updates an eICR bundle record.
	 *
	 * @param dafBundle the bundle to persist
	 */
	void createBundle(DafBundle dafBundle);
}
