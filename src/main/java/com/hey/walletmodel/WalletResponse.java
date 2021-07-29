package com.hey.walletmodel;

public class WalletResponse {
	private String hashedPin;
	public String getHashedPin() {
		return hashedPin;
	}

	public void setHashedPin(String hashedPin) {
		this.hashedPin = hashedPin;
	}
	private Long balance;

	public Long getBalance() {
		return balance;
	}
	
	public void setBalance(Long balance) {
		this.balance = balance;
	}
	public WalletResponse() {}
}
