package com.hey.model;

public class ReceiveMoneyMessage extends WsMessage{
	private String sender;
	private UserHash userHash;
	public UserHash getUserHash() {
		return userHash;
	}
	public void setUserHash(UserHash userHash) {
		this.userHash = userHash;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String username) {
		this.sender = username;
	}
	private Long amount;
	private Long timestamp;
	
	public Long getAmount() {
		return amount;
	}
	public void setAmount(Long amount) {
		this.amount = amount;
	}
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
}
// vay la duoc dieu nay cung don gian cai thu 2 neu ma thang kia ko co online??
// khi ma vau access vao thi no can lam cai dieu do ??
// cai thu 2 la viec minh ne ??
