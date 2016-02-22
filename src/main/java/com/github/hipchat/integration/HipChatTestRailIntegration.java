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
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.groovy.control.CompilationFailedException;

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
			return createResponse(request2TestRail(createGroovyScript(convert(message.message))));
		}
		catch (Exception e) {
			return createErrorResponse(e.getMessage());
		}
	}

	private HipChatResponse createErrorResponse(String message) {
		HipChatResponse response = new HipChatResponse();
		response.color = "red";
		response.message = message;
		return response;
	}

	private HipChatResponse createResponse(List<Object> evaluate) {
		HipChatResponse response = new HipChatResponse();
		response.color = "green";
		response.message = evaluate.stream().map(Objects::toString).collect(Collectors.joining(", "));
		return response;
	}

	@SuppressWarnings("unchecked")
	private List<Object> request2TestRail(File tempFile) throws CompilationFailedException, IOException {
		return (List<Object>) new Runner().evaluate(tempFile);
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
		lines.add(message);
		Files.write(tempFile.toPath(), lines);
		return tempFile;
	}
}
