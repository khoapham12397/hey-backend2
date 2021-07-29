package com.hey.walletmodel;

import java.util.List;

public class PresentsOfSession {
	
	private String sessionId;
	
	private List<Present> present;

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public List<Present> getPresent() {
		return present;
	}

	public void setPresent(List<Present> present) {
		this.present = present;
	}
	
	
}
