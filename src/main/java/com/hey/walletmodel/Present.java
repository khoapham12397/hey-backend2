package com.hey.walletmodel;

public class Present {
	// present info xoa di la duioc ??
	// dung vay dpo:
	
	private String userId;
	private Long totalAmount;
	private String sessionId;
	private Long startTime;
	private String presentId;
	private Boolean expired;
	private Long envelope;
	private Boolean equal;

	public String getPresentId() {return presentId;}
	public void setPresentId(String presentId) {this.presentId = presentId;}

	public Present() {}
	
	public String getUserId() {return userId;}
	public void setUserId(String userId) {this.userId = userId;}
	
	public Long getTotalAmount() {return totalAmount;}
	public void setTotalAmount(Long totalAmount) {this.totalAmount = totalAmount;}

	public String getSessionId() {return sessionId;}
	public void setSessionId(String sessionId) {this.sessionId = sessionId;}

	public Long getStartTime() {return startTime;}
	public void setStartTime(Long startTime) {this.startTime = startTime;}

	public Boolean getExpired() {return expired;}
	public void setExpired(Boolean expired) {this.expired = expired;}

	public Long getEnvelope(){return envelope;}
	public void setEnvelope(Long envelope){this.envelope=envelope;}

	public Boolean getEqual(){return equal;}
	public void setEqual(Boolean equal){this.equal=equal;}
}
