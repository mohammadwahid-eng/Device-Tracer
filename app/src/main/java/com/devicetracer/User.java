package com.devicetracer;

import android.net.Uri;

public class User {
	private String uid;
	private String name;
	private String phone;
	private String imei;
	private String photo;

	public User() {
	}

	public User(String uid, String name, String phone, String imei, String photo) {
		this.uid = uid;
		this.name = name;
		this.phone = phone;
		this.imei = imei;
		this.photo = photo;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
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