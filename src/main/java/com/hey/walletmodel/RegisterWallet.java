package com.hey.walletmodel;

import java.io.Serializable;

public class RegisterWallet implements Serializable{
	
	private String hashedPin;
	private String phone; 
	private String identity;
	private String email;
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getHashedPin() {
		return hashedPin;
	}

	public void setHashedPin(String hashedPin) {
		this.hashedPin = hashedPin;
	}
}
