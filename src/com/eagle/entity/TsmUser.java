package com.eagle.entity;

import java.io.Serializable;

public class TsmUser implements Serializable{
	
	private static final long serialVersionUID = -6591514840729413504L;
	
	private String userId;
	
	private String userName;
	
	private String email;
	
	private String tel;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}
	
}
