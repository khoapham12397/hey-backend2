package com.hey.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


public class ChatMessage implements Serializable {
    public static int USER_MESSAGE_TYPE = 0;
	public static int NOTIFICATION_PRESENT_TYPE =1;
	public static int NOTIFICATION_P2P_TYPE =2;
	// khi ma return ve no ra cai fi ?
	
	private String sessionId;
    private UserHash userHash;
    private String message;
    private Date createdDate;
    private int msgType;
    
    public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int type) {
		this.msgType = type;
	}

	public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public UserHash getUserHash() {
        return userHash;
    }

    public void setUserHash(UserHash userHash) {
        this.userHash = userHash;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}
