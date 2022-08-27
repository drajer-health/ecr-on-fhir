package com.drajer.eicrresponder.service.impl;

import java.security.KeyStoreException;
import java.util.Objects;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.drajer.eicrresponder.model.Response;
import com.drajer.eicrresponder.util.CommonUtil;

@Service
public class GenerateAccessToken {
	private static final Logger logger = LoggerFactory.getLogger(GenerateAccessToken.class);
	
	 /**
	   * @param signed jwt token
	   * @return the token response from the auth server
	   * @throws KeyStoreException in case of invalid signed private keys
	   */
	  public JSONObject getAccessToken(String jwtSignToken) throws KeyStoreException {
	    RestTemplate resTemplate = new RestTemplate();
	    String tokenEndpoint = CommonUtil.getProperty("jwt.accesstoken.endpoint");
	    String clientId = CommonUtil.getProperty("jwt.accesstoken.client");
	    logger.info("client id:::::"+tokenEndpoint);

	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

	    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
	    map.add("grant_type", "client_credentials");
	    map.add("client_id", clientId);
	    map.add("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer");
	    map.add("client_assertion", jwtSignToken);
	    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
	    ResponseEntity<?> response = resTemplate.postForEntity(tokenEndpoint, request, Response.class);
	    return new JSONObject(Objects.requireNonNull(response.getBody()));
	  }
}
