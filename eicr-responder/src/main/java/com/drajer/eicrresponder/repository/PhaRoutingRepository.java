package com.drajer.eicrresponder.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.drajer.eicrresponder.entity.PhaRouting;

public interface PhaRoutingRepository extends JpaRepository<PhaRouting, Long> {

	/**
	 * @param phaAgencyCode
	 * @return List<PhaRouting> list of pharouting
	 */
	@Query("select p from PhaRouting p where p.phaAgencyCode = :phaAgencyCode")
	List<PhaRouting> findByAgencyCode(@Param("phaAgencyCode") String phaAgencyCode);

}
