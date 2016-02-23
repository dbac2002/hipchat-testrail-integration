package com.github.hipchat.integration;

public class Converter {
	private Converter() {
		// static only
	}

	public static String convert(String hipChatMessage) {
		return hipChatMessage.substring("/testrail ".length());
	}
}
