package com.devicetracer;

public class DeviceData {
	private double Latitude;
	private double Longitude;
	private long time;

	public DeviceData() {
	}

	public DeviceData(double latitude, double longitude, long time) {
		Latitude = latitude;
		Longitude = longitude;
		this.time = time;
	}

	public double getLatitude() {
		return Latitude;
	}

	public void setLatitude(double latitude) {
		Latitude = latitude;
	}

	public double getLongitude() {
		return Longitude;
	}

	public void setLongitude(double longitude) {
		Longitude = longitude;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
}
