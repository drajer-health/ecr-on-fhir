package com.drajer.fhir.router.service;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class PrivateKeyGenerator {
	private static final Logger logger = LoggerFactory.getLogger(PrivateKeyGenerator.class);

	public String createJwtSignedHMAC(String jwtPkeyAudience, String privateKeyString, String clientId)
			throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
		PrivateKey privateKey = getPrivateKey(privateKeyString);
		String jwtToken = null;
		Instant now = Instant.now();
		logger.info("Audience :::: "+jwtPkeyAudience);
		String jwtPkeySubject = clientId; // "fhir-router";
		String jwtPkeyIssuer = clientId; //"fhir-router";
		logger.info("subject :::: "+jwtPkeySubject);
		logger.info("issuer :::: "+jwtPkeyIssuer);
		 jwtToken = Jwts.builder()
				.setAudience(jwtPkeyAudience)
				.setId(UUID.randomUUID().toString())
				.setSubject(jwtPkeySubject)
				.setIssuer(jwtPkeyIssuer)
				.setIssuedAt(Date.from(now))
				.setExpiration(Date.from(now.plus(30l, ChronoUnit.MINUTES)))
				.signWith(privateKey, SignatureAlgorithm.RS256).compact();
		return jwtToken;
	}

	private static PrivateKey getPrivateKey(String rsaPrivateKey) {
		PrivateKey privKey = null;
		try {
			rsaPrivateKey = rsaPrivateKey.replace("-----BEGIN PRIVATE KEY-----", "");
			rsaPrivateKey = rsaPrivateKey.replace("\n", "");
			rsaPrivateKey = rsaPrivateKey.replace("-----END PRIVATE KEY-----", "");
			rsaPrivateKey = rsaPrivateKey.replaceAll("\\s+", "");
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(rsaPrivateKey));
			KeyFactory kf = KeyFactory.getInstance("RSA");
			privKey = kf.generatePrivate(keySpec);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return privKey;
	}
}
