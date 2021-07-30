package com.hey.walletmodel;

public class GetBalanceResponse {
	private Long balance;
	private boolean code;
	public boolean isCode() {
		return code;
	}

	public void setCode(boolean code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	private String message;
	public Long getBalance() {
		return balance;
	}

	public void setBalance(Long balance) {
		this.balance = balance;
	}
}
