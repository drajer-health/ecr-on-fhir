package com.drajer.eicrresponder.model;

public class Jurisdiction {
	private String phaCode;
	private String phaEndpointUrl;

	public String getPhaCode() {
		return phaCode;
	}

	public void setPhaCode(String phaCode) {
		this.phaCode = phaCode;
	}

	public String getPhaEndpointUrl() {
		return phaEndpointUrl;
	}

	public void setPhaEndpointUrl(String phaEndpointUrl) {
		this.phaEndpointUrl = phaEndpointUrl;
	}

	@Override
	public String toString() {
		return "Jurisdiction [phaCode=" + phaCode + ", phaEndpointUrl=" + phaEndpointUrl + "]";
	}
}
