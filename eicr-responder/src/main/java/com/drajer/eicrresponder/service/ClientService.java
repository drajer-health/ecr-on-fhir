package com.drajer.eicrresponder.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.drajer.eicrresponder.entity.PhaRouting;
import com.drajer.eicrresponder.util.CommonUtil;

/**
 * @author Girish Rao
 *
 */
@Service
public class ClientService {

	private static final Logger logger = LoggerFactory.getLogger(ClientService.class);

	private final WebClient webClient;

	/**
	 * @param webClientBuilder
	 */
	public ClientService(WebClient.Builder webClientBuilder) {
		String phaUiEndPoint = CommonUtil.getProperty("responder.endpoint");
		logger.info("phiuiEndpoint ::::"+phaUiEndPoint);
		this.webClient = webClientBuilder.baseUrl(phaUiEndPoint).build();
	}

	/**
	 * @param phaAgencyCode
	 * @return PhaRouting
	 */
	public PhaRouting getPhaUrl(String phaAgencyCode) {
		PhaRouting phaRouting = null;
		PhaRouting[] phaRoutings = webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/phaByAgency/{phaAgencyCode}").build(phaAgencyCode)).retrieve()
				.bodyToMono(PhaRouting[].class).block();

		if (phaRoutings.length > 0) {
			logger.info("EndpointUrl  :::::" + phaRoutings[0].getEndpointUrl());
			phaRouting = phaRoutings[0];
		}
		return phaRouting;
	}
}
