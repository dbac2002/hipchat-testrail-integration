package com.github.hipchat.integration;

import static com.github.hipchat.integration.Converter.convert;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.github.groovyclient.Runner;
import com.github.hipchat.integration.model.HipChatRequest;
import com.github.hipchat.integration.model.HipChatRequestMessage;
import com.github.hipchat.integration.model.HipChatResponse;
import com.google.gson.Gson;

@Path("toHipChat")
public class HipChatTestRailIntegration {
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public HipChatResponse groovy(String request) {
		try {
			HipChatRequestMessage message = new Gson().fromJson(request, HipChatRequest.class).item.message;
			Object request2TestRail = request2TestRail(createGroovyScript(convert(message.message)));
			return createResponse(parseTestRailResponse(request2TestRail));
		}
		catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse(e.getMessage());
		}
	}

	private String[] parseTestRailResponse(Object request2TestRail) {
		if (request2TestRail instanceof List<?>) {
			List<?> list = (List<?>) request2TestRail;
			String resp = "Total: " + list.size() + " " + (list.size() == 1 ? "entry" : "entries") + "\n";
			return new String[] { resp + list.stream().map(Objects::toString).collect(Collectors.joining("\n")),
					"green" };
		}
		if (request2TestRail instanceof Map<?, ?>) {
			Map<?, ?> map = (Map<?, ?>) request2TestRail;
			String color = "green";
			Object o = map.get("passed_percentage");
			if (o != null) {
				Integer per = Integer.parseInt(String.valueOf(map.get("passed_percentage")));
				int iper = per.intValue();
				if (iper < 30) {
					color = "red";
				}
				else if (iper < 60) {
					color = "yellow";
				}
			}

			return new String[] { request2TestRail.toString(), color };
		}
		return new String[] { request2TestRail.toString(), "green" };
	}

	private HipChatResponse createErrorResponse(String message) {
		HipChatResponse response = new HipChatResponse();
		response.color = "red";
		response.message = message;
		return response;
	}

	private HipChatResponse createResponse(String[] res) {
		HipChatResponse response = new HipChatResponse();
		response.color = res[1];
		response.message = res[0];
		return response;
	}

	private Object request2TestRail(File tempFile) throws IOException {
		return new Runner().evaluate(tempFile);
	}

	private File createGroovyScript(String message) throws IOException {
		File tempFile = File.createTempFile("groovy-script", ".groovy");
		tempFile.deleteOnExit();

		List<String> templateLines = new ArrayList<>();

		try (InputStream resource = HipChatTestRailIntegration.class.getResourceAsStream("/groovyclient.template")) {
			templateLines.addAll(new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8)).lines()
					.collect(Collectors.toList()));
		}
		if (templateLines.isEmpty()) {
			throw new IllegalArgumentException("Could not load template file or is empty");
		}
		List<String> lines = new ArrayList<>(templateLines);

		if (message.contains("with")) {
			lines.add(message);
		}
		else {
			lines.add("def result = " + message);
			lines.add("result.json()");
		}
		System.out.println(lines);

		Files.write(tempFile.toPath(), lines);
		return tempFile;
	}
}
