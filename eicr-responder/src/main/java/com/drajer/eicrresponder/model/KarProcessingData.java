package com.drajer.eicrresponder.model;

public class KarProcessingData {

	
	  /**
	   * The healthcare setting which will provide the necessary data to access for applying the Kar.
	   */
	  HealthcareSetting healthcareSetting;
	  
	  /** The context data that was received via notification. */
	  NotificationContext notificationContext;

	public HealthcareSetting getHealthcareSetting() {
		return healthcareSetting;
	}

	public void setHealthcareSetting(HealthcareSetting healthcareSetting) {
		this.healthcareSetting = healthcareSetting;
	}

	public NotificationContext getNotificationContext() {
		return notificationContext;
	}

	public void setNotificationContext(NotificationContext notificationContext) {
		this.notificationContext = notificationContext;
	}	


}
