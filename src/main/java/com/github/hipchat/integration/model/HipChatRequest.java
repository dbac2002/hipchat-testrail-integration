package com.github.hipchat.integration.model;

public class HipChatRequest {
	public String event;
	public String oauth_client_id;
	public String webhook_id;
	public HipChatRequestItem item;
	
	@Override
	public String toString() {
		return "HipChatRequest [event=" + event + ", oauth_client_id="
				+ oauth_client_id + ", webhook_id=" + webhook_id + ", item="
				+ item + "]";
	}		
}
