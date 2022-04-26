package com.drajer.eicrresponder.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.drajer.eicrresponder.entity.PhaRouting;
import com.drajer.eicrresponder.repository.PhaRoutingRepository;

/**
 * @author Girish Rao 
 * Controller for pha routing
 */
@RestController
@CrossOrigin
@RequestMapping("/api")
public class PhaRoutingController {

	@Autowired
	private PhaRoutingRepository phaRoutingRepository;

	// GET method to fetch all pha routings
	/**
	 * @return
	 */
	@GetMapping("/phalists")
	public List<PhaRouting> getAllPhaRoutings() {
		return phaRoutingRepository.findAll();
	}

	// GET method to fetch pha routing by Id
	/**
	 * @param phaRoutingId
	 * @return ResponseEntity<PhaRouting>
	 * @throws Exception
	 */
	@GetMapping("/pha/{id}")
	public ResponseEntity<PhaRouting> getPhaRoutingById(@PathVariable(value = "id") Long phaRoutingId)
			throws Exception {
		PhaRouting phaRouting = phaRoutingRepository.findById(phaRoutingId)
				.orElseThrow(() -> new Exception("PHA Routing " + phaRoutingId + " not found"));
		return ResponseEntity.ok().body(phaRouting);
	}

	// GET method to fetch pha routing by Id
	/**
	 * @param phaAgencyCode
	 * @return List<PhaRouting>
	 * @throws Exception
	 */
	@GetMapping("/phaByAgency/{phaAgencyCode}")
	public List<PhaRouting> getPhaRoutingById(@PathVariable(value = "phaAgencyCode") String phaAgencyCode)
			throws Exception {
		List<PhaRouting> phaRoutings = phaRoutingRepository.findByAgencyCode(phaAgencyCode);
		return phaRoutings;
	}

	// POST method to create a pha routing
	/**
	 * @param phaRouting
	 * @return PhaRouting
	 */
	@PostMapping("/pha")
	public PhaRouting createPhaRouting(@Valid @RequestBody PhaRouting phaRouting) {
		return phaRoutingRepository.save(phaRouting);
	}

	// PUT method to update a pha routing's details
	/**
	 * @param phaRoutingId
	 * @param phaRoutingDetails
	 * @return ResponseEntity<PhaRouting>
	 * @throws Exception
	 */
	@PutMapping("/pha/{id}")
	public ResponseEntity<PhaRouting> updatePhaRouting(@PathVariable(value = "id") Long phaRoutingId,
			@Valid @RequestBody PhaRouting phaRoutingDetails) throws Exception {
		PhaRouting phaRouting = phaRoutingRepository.findById(phaRoutingId)
				.orElseThrow(() -> new Exception("PHA Routing " + phaRoutingId + " not found"));

		phaRouting.setPhaAgencyCode(phaRoutingDetails.getPhaAgencyCode());
		phaRouting.setReceiverProtocol(phaRoutingDetails.getReceiverProtocol());
		phaRouting.setProtocolType(phaRoutingDetails.getProtocolType());
		phaRouting.setEndpointUrl(phaRoutingDetails.getEndpointUrl());

		final PhaRouting UpdatePhaRouting = phaRoutingRepository.save(phaRouting);
		return ResponseEntity.ok(UpdatePhaRouting);
	}

	// DELETE method to delete a PhaRouting
	/**
	 * @param phaRoutingId
	 * @return Map<String, Boolean>
	 * @throws Exception
	 */
	@DeleteMapping("/pha/{id}")
	public Map<String, Boolean> deletePharouting(@PathVariable(value = "id") Long phaRoutingId) throws Exception {
		PhaRouting phaRouting = phaRoutingRepository.findById(phaRoutingId)
				.orElseThrow(() -> new Exception("Pha Routing " + phaRoutingId + " not found"));

		phaRoutingRepository.delete(phaRouting);
		Map<String, Boolean> response = new HashMap<>();
		response.put("deleted", Boolean.TRUE);
		return response;
	}
}