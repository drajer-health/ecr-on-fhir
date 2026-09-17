package org.sitenv.spring.service;

import org.sitenv.spring.model.DafBundle;

/**
 * Service contract for {@link DafBundle} (eICR) records.
 */
public interface BundleService {

	/**
	 * Saves or updates an eICR bundle record.
	 *
	 * @param dafBundle the bundle to persist
	 */
	void createBundle(DafBundle dafBundle);
}
