package com.drajer.eicrresponder.model;

import java.util.Date;

public class NotificationContext {

	private String ehrAccessToken;
	private int ehrAccessTokenExpiryDuration;
	private Date ehrAccessTokenExpirationTime;
	
	public String getEhrAccessToken() {
		return ehrAccessToken;
	}
	public void setEhrAccessToken(String ehrAccessToken) {
		this.ehrAccessToken = ehrAccessToken;
	}
	public int getEhrAccessTokenExpiryDuration() {
		return ehrAccessTokenExpiryDuration;
	}
	public void setEhrAccessTokenExpiryDuration(int ehrAccessTokenExpiryDuration) {
		this.ehrAccessTokenExpiryDuration = ehrAccessTokenExpiryDuration;
	}
	public Date getEhrAccessTokenExpirationTime() {
		return ehrAccessTokenExpirationTime;
	}
	public void setEhrAccessTokenExpirationTime(Date ehrAccessTokenExpirationTime) {
		this.ehrAccessTokenExpirationTime = ehrAccessTokenExpirationTime;
	}

}
