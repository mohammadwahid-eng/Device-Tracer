package com.devicetracer;

import android.location.Location;

public class DeviceData {
	private Location location;
	public DeviceData() {

	}

	public DeviceData(Location location) {
		this.location = location;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
}
