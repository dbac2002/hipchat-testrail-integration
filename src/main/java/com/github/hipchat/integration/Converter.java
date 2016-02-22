package com.github.hipchat.integration;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Converter {
	private static final String SIMPLE_GET = "get(%s) of %s(%s)";
	private static final Pattern SIMPLE_ID_PATTERN = Pattern.compile("/testrail get (.+) of ([a-zA-Z]+) (\\d+)");
	private static final Pattern SIMPLE_ID_EMAIL = Pattern.compile("/testrail get (.+) of ([a-zA-Z]+) (.+@.+)");

	private Converter() {
		// static only
	}

	public static String convert(String hipChatMessage) {
		Matcher m = SIMPLE_ID_PATTERN.matcher(hipChatMessage);
		if (m.find()) {
			String properties = parseProperties(m.group(1));
			String type = m.group(2);
			String id = m.group(3);
			return String.format(SIMPLE_GET, properties, type, id);
		}
		m = SIMPLE_ID_EMAIL.matcher(hipChatMessage);
		if (m.find()) {
			String properties = parseProperties(m.group(1));
			String type = m.group(2);
			String id = m.group(3);
			return String.format(SIMPLE_GET, properties, type, "'" + id + "'");
		}
		return hipChatMessage.substring("/testrail ".length());
	}

	private static String parseProperties(String group) {
		String[] split = group.split(",");
		if (split.length == 1) {
			return "'" + group + "'";
		}
		return stream(split).map(p -> "'" + p.trim() + "'").collect(joining(","));
	}
}
