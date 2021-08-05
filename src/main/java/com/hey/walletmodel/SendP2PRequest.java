package com.hey.walletmodel;

public class SendP2PRequest {
	
	private String userId;
	private Long amount;
	private String pin;
	private String message;
	private String name;
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public Long getAmount() {
		return amount;
	}
	public void setAmount(Long amount) {
		this.amount = amount;
	}
	public String getPin() {
		return pin;
	}
	public void setPin(String pin) {
		this.pin = pin;
	}
	public String getName(){return name;}
	public void setName(String name){this.name = name;}
	public SendP2PRequest() {}
}
