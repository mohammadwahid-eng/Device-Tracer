package com.devicetracer;

public class User {
	private String name;
	private String mobile;
	private String imei;
	private String photo;

	public User() {
	}

	public User(String name, String mobile, String imei, String photo) {
		this.name = name;
		this.mobile = mobile;
		this.imei = imei;
		this.photo = photo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}
}