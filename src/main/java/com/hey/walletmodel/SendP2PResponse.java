package com.hey.walletmodel;

public class SendP2PResponse {
	private Boolean code;
	public Boolean getCode() {
		return code;
	}
	public void setCode(Boolean code) {
		this.code = code;
	}
	private String message;
	private String transactionId;
	private Long timestamp;
	private Long amount;
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	public Long getAmount(){return amount;}
	public void setAmount(Long amount){this.amount=amount;}
}
