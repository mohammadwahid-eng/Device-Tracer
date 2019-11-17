package com.devicetracer;

public class User {

	private String name;
	private String email;
	private String imei;
	private String mobile;
	private String photo;
	public User() {
	}

	public User(String name, String email, String mobile, String imei, String photo) {
		this.name = name;
		this.email = email;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String emil) {
		this.email = emil;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getPhoto() { return photo; }

	public void setPhoto(String photo) { this.photo = photo; }
}