package com.drajer.eicrresponder.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
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

import com.drajer.eicrresponder.util.CommonUtil;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class PrivateKeyGenerator {
	private static final Logger logger = LoggerFactory.getLogger(PrivateKeyGenerator.class);

	public String createJwtSignedHMAC(File file)
			throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
		PrivateKey privateKey = getPrivateKey(file);
		String jwtToken = null;
		Instant now = Instant.now();
		String jwtPkeyAudience = CommonUtil.getProperty("jwt.prikey.audience");
		String jwtPkeySubject = CommonUtil.getProperty("jwt.prikey.subject");
		String jwtPkeyIssuer = CommonUtil.getProperty("jwt.prikey.issuer");
		logger.info("Audience :::: "+jwtPkeyAudience);
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

	private static PrivateKey getPrivateKey(File file) {
		PrivateKey privKey = null;
		try {
			String rsaPrivateKey = new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());
			rsaPrivateKey = rsaPrivateKey.replace("-----BEGIN PRIVATE KEY-----", "");
			rsaPrivateKey = rsaPrivateKey.replace("\n", "");
			rsaPrivateKey = rsaPrivateKey.replace("-----END PRIVATE KEY-----", "");
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(rsaPrivateKey));
			KeyFactory kf = KeyFactory.getInstance("RSA");
			privKey = kf.generatePrivate(keySpec);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return privKey;
	}
}
