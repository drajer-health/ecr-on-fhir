package com.drajer.eicrresponder.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.drajer.eicrresponder.model.Jurisdiction;
import com.drajer.eicrresponder.model.PhaRoutingResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ProcessJurisdictions {
	private static final Logger logger = LoggerFactory.getLogger(ProcessJurisdictions.class);
	private final RestTemplate restTemplate;
	private final String FIND_BY_JURISIDICTION ="phaByAgency/";
	
	public ProcessJurisdictions(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}


	public NodeList getNodeList(NodeList nodeList, String nodeName) {
		for (int k = 0; k < nodeList.getLength(); k++) {
			Node node = nodeList.item(k);
			if (node.getNodeName().equalsIgnoreCase(nodeName)) {
				return node.getChildNodes();
			}
		}
		return null;
	}
		
	public Jurisdiction getJurisdiction(String stateCode) {
		Jurisdiction jurisdiction = new Jurisdiction();
		jurisdiction.setPhaCode(stateCode);
		jurisdiction.setPhaEndpointUrl(StringUtils.EMPTY);
		String apiEndPointUrl = CommonUtil.getProperty("responder.endpoint");
		logger.info("call pha agency get endpoint url::::" + apiEndPointUrl + " ::::: " + stateCode);
		ResponseEntity<String> responseEntity = phaUrlByAgencyCode(apiEndPointUrl + FIND_BY_JURISIDICTION + stateCode);
		if (!responseEntity.getBody().isEmpty()) {
			jurisdiction.setPhaEndpointUrl(responseEntity.getBody().toString());
		}
		logger.info("getJurisdiction return obj :::::"+jurisdiction);
		return jurisdiction;
	}

	public ResponseEntity<String> phaUrlByAgencyCode(String endPointUrl) {
		logger.info("phaUrlByAgencyCode.......");
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		ResponseEntity<String> returnResponse = null;
		try {
			logger.info("endPointUrl::::" + endPointUrl);
			String json = restTemplate.getForObject(endPointUrl, String.class);
			logger.info("json::::" + json);
			// ObjectMapper instantiation
			ObjectMapper objectMapper = new ObjectMapper();

			// Deserialization into the `PhaRoutingResponse` class
			PhaRoutingResponse[] phaRoutingResponses = objectMapper.readValue(json, PhaRoutingResponse[].class);

			logger.info("phaRouting length::::" + phaRoutingResponses.length);
			String phaEndPointUrl = "";
			if (phaRoutingResponses.length > 0) {
				PhaRoutingResponse phaRoutingResponse = phaRoutingResponses[0];
				phaEndPointUrl = phaRoutingResponse.getEndpointUrl();
				logger.info("phaRoutingResponse getEndpointUrl ::::" + phaEndPointUrl);
			}

			returnResponse = new ResponseEntity<String>(phaEndPointUrl, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error in getting pha agency code information for URL::::: {}", endPointUrl, e.getMessage());
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		return returnResponse;
	}

}
