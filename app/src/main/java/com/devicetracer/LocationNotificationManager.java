package com.devicetracer;

public class LocationNotificationManager {
	private String sender;
	private String type;
	private long time;

	public LocationNotificationManager() {
	}

	public LocationNotificationManager(String sender, String type, long time) {
		this.sender = sender;
		this.type = type;
		this.time = time;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
}
