package com.hey.walletmodel;

public class Profile {
	String userId;
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	private String phone;
	private String fullName;
	private String address;
	private String identityNumber;
	private Boolean gender;
	
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getIdentityNumber() {
		return identityNumber;
	}
	public void setIdentityNumber(String identityNumber) {
		this.identityNumber = identityNumber;
	}
	
	public Boolean getGender() {
		return gender;
	}
	public void setGender(Boolean gender) {
		this.gender = gender;
	}
	public Profile(String phone, String fullName, String address, String identityNumber,
			Boolean gender) {
		super();
		this.phone = phone;
		this.fullName = fullName;
		this.address = address;
		this.identityNumber = identityNumber;
		this.gender = gender;
	}
	public Profile() {}
}

