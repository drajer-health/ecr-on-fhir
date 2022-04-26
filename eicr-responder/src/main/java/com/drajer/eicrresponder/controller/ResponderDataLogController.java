package com.drajer.eicrresponder.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.drajer.eicrresponder.entity.ResponderDataLog;
import com.drajer.eicrresponder.repository.ResponderDataLogRepository;

/**
 * @author Girish Rao Controller for ResponderEicrDataLogController
 */
@RestController
@CrossOrigin
@RequestMapping("/api")
public class ResponderDataLogController {

	@Autowired
	private ResponderDataLogRepository responderDataLogRepository;

	// GET method to fetch all Responder Data Log
	/**
	 * @return
	 *
	 */
	@GetMapping("/responderlogs")
	public List<ResponderDataLog> getAllResponderEicrDataLog() {
		return responderDataLogRepository.findAll();
	}

	// GET method to fetch ResponderDataLog routing by Id
	/**
	 * @param phaRoutingId
	 * @return ResponseEntity<ResponderDataLog>
	 * @throws Exception
	 */
	@GetMapping("/responderlog/{id}")
	public ResponseEntity<ResponderDataLog> getResponderlogById(@PathVariable(value = "id") Long responderlogId)
			throws Exception {
		ResponderDataLog responderDataLog = responderDataLogRepository.findById(responderlogId)
				.orElseThrow(() -> new Exception("Responder Log " + responderlogId + " not found"));
		return ResponseEntity.ok().body(responderDataLog);
	}

	// POST method to create a ResponderDataLog
	/**
	 * @param ResponderDataLog
	 * @return ResponderDataLog
	 */
	@PostMapping("/responderlog")
	public ResponderDataLog createResponderlog(@Valid @RequestBody ResponderDataLog responderDatalog) {
		ResponderDataLog respDataLog = responderDatalog;
		try {
			respDataLog = responderDataLogRepository.save(responderDatalog);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return respDataLog;
	}
}