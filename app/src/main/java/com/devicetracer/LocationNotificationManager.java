package com.devicetracer;

public class LocationNotificationManager {
	private long notificationID;
	private String senderID;
	private String type;
	private long time;
	private boolean notificationSeen;

	public LocationNotificationManager() {
	}

	public LocationNotificationManager(long notificationID, String senderID, String type, long time, boolean notificationSeen) {
		this.notificationID = notificationID;
		this.senderID = senderID;
		this.type = type;
		this.time = time;
		this.notificationSeen = notificationSeen;
	}

	public long getNotificationID() {
		return notificationID;
	}

	public void setNotificationID(long notificationID) {
		this.notificationID = notificationID;
	}

	public String getSenderID() {
		return senderID;
	}

	public void setSenderID(String senderID) {
		this.senderID = senderID;
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

	public boolean isNotificationSeen() {
		return notificationSeen;
	}

	public void setNotificationSeen(boolean notificationSeen) {
		this.notificationSeen = notificationSeen;
	}
}
