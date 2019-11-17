package com.devicetracer;

public class DeviceLocation {
	private float accuracy;
	private double latitude;
	private double longitude;
	private long time;

	public DeviceLocation() {
	}

	public DeviceLocation(float accuracy, double latitude, double longitude, long time) {
		this.accuracy = accuracy;
		this.latitude = latitude;
		this.longitude = longitude;
		this.time = time;
	}

	public float getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(float accuracy) {
		this.accuracy = accuracy;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
}
