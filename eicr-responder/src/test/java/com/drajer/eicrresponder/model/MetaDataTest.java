package com.drajer.eicrresponder.model;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

class MetaDataTest {
	MetaData metaData = new MetaData();

	@BeforeEach
	void init() {
		metaData.setMessageId("1242343");
		metaData.setSenderUrl("http://");
		List<Jurisdiction> jurisdictions = new ArrayList<Jurisdiction>();
		Jurisdiction jurisdiction = new Jurisdiction();
		jurisdiction.setPhaCode("NY");
		jurisdiction.setPhaEndpointUrl("http://localhost:8080/eicrresponder/api/v1/upload");
		jurisdictions.add(jurisdiction);

		jurisdiction = new Jurisdiction();
		jurisdiction.setPhaCode("MD");
		jurisdiction.setPhaEndpointUrl("http://localhost:8080/eicrresponder/api/v1/upload");
		jurisdictions.add(jurisdiction);

		jurisdiction = new Jurisdiction();
		jurisdiction.setPhaCode("VA");
		jurisdiction.setPhaEndpointUrl("http://localhost:8080/eicrresponder/api/v1/upload");
		jurisdictions.add(jurisdiction);

		metaData.setJurisdictions(jurisdictions);
	}

	@AfterEach
	void teardown() {
		metaData = null;
	}

	@Test
	void testGetMessageId() {
		assertNotNull(metaData.getMessageId());
	}

	@Test
	void testGetSenderUrl() {
		assertNotNull(metaData.getSenderUrl());
	}

	@Test
	void testGetJurisdictions() {
		assertNotNull(metaData.getJurisdictions());

		// create ObjectMapper instance
		ObjectMapper objectMapper = new ObjectMapper();

		// configure objectMapper for pretty input
		objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);

		// write MetaData object to metaData.json file
		try {
			objectMapper.writeValue(new File("MetaData.json"), metaData);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
