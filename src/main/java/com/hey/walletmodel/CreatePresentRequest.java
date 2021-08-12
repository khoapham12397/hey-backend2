package com.hey.walletmodel;

public class CreatePresentRequest {
	private String sessionId;
	private Long amount;
	private String pin;
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public Long getAmount() {
		return amount;
	}
	public void setAmount(Long amount) {
		this.amount = amount;
	}
	public String getPin(){
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}
}
