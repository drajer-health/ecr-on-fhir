package com.drajer.fhir.router.service;

import java.security.KeyStoreException;
import java.util.Objects;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.drajer.fhir.router.model.Response;

@Service
public class GenerateAccessToken {
	@Autowired
	private PrivateKeyGenerator privateKeyGenerator;
	private static final Logger logger = LoggerFactory.getLogger(GenerateAccessToken.class);
	
	 /**
	   * @param signed jwt token
	   * @return the token response from the auth server
	   * @throws KeyStoreException in case of invalid signed private keys
	   */
	  public JSONObject getAccessToken(String jwtSignToken, String tokenEndpoint, String clientId) throws KeyStoreException {
	    RestTemplate resTemplate = new RestTemplate();
	    logger.info("client id:::::"+clientId);
	    logger.info("tokenEndpoint :::::"+tokenEndpoint);
	    logger.info("jwtSignToken :::::"+jwtSignToken);
	    String jwtSignTokenToSend = null;
	    try {
			jwtSignTokenToSend = privateKeyGenerator.createJwtSignedHMAC(tokenEndpoint,jwtSignToken, clientId);
		} catch (Exception e) {
			logger.error("privateKeyGenerator  createJwtSignedHMAC :"+e.getMessage());
			e.printStackTrace();
		}
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

	    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
	    map.add("grant_type", "client_credentials");
	    map.add("client_id", clientId);
	    map.add("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer");
	    map.add("client_assertion", jwtSignTokenToSend);
	    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
	    ResponseEntity<?> response = resTemplate.postForEntity(tokenEndpoint, request, Response.class);
	    logger.info("Access Token response :"+response.toString());
	    return new JSONObject(Objects.requireNonNull(response.getBody()));
	  }
}
