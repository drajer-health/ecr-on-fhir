package com.drajer.fhir.router.service;

import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

public class WebClient {

	public String putToPha(String url, String jwtToken, String requestBody) {
		String responseObj = null;
		try {
			// URL where the request will be forwarded
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost putRequest = new HttpPost(url);
			putRequest.setHeader("Authorization", jwtToken);
			putRequest.setHeader("X-Request-ID", UUID.randomUUID().toString());
			StringEntity params = new StringEntity(requestBody);

			putRequest.setEntity(params);

			HttpResponse response = httpClient.execute(putRequest);
			System.out.println("Response Code::::" + response.getStatusLine().getStatusCode());
			responseObj = response.toString();
			System.out.println("responseObj String ::::" + responseObj);

		} catch (Exception ex) {
			responseObj = ex.getMessage();
			ex.printStackTrace();
		}
		return responseObj;
	}

}
