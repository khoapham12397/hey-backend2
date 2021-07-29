package com.hey.model;

public class ChatContainerRequest extends WsMessage {
    private String sessionId;
    // thuc te cai sessionId cua chat Con
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
