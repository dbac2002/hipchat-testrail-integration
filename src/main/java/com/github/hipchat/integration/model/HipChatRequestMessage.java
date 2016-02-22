package com.github.hipchat.integration.model;

public class HipChatRequestMessage {
	public String date;
	public String id;
	public String message;
	public String type;

	@Override
	public String toString() {
		return "HipChatRequestMessage [date=" + date + ", id=" + id + ", message=" + message + ", type=" + type + "]";
	}
}
