package com.hey.model;

public class ChatMessageSendP2P extends ChatMessageRequest{
	private boolean sendP2P;

	public boolean isSendP2P() {
		return sendP2P;
	}

	public void setSendP2P(boolean sendP2P) {
		this.sendP2P = sendP2P;
	}
}
