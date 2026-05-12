package com.uq.happypet.testrail;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import tools.jackson.databind.json.JsonMapper;

@Component
public class TestRailRestClient {

	private static final Logger log = LoggerFactory.getLogger(TestRailRestClient.class);

	private static final int BODY_PREVIEW_MAX = 500;

	private final TestRailProperties properties;
	private final JsonMapper jsonMapper;
	private final HttpClient httpClient;

	public TestRailRestClient(TestRailProperties properties, JsonMapper jsonMapper) {
		this.properties = properties;
		this.jsonMapper = jsonMapper;
		int timeoutSec = Math.max(1, properties.getRequestTimeoutSeconds());
		this.httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(Math.min(30, timeoutSec)))
				.build();
	}

	public void addResult(long runId, long caseId, int statusId, String commentOrNull)
			throws IOException, InterruptedException {
		URI uri = TestRailUriSupport.addResultUri(properties.getUrl(), runId);
		String auth = buildBasicAuthorization(properties.getUser(), properties.getApiKey());
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("case_id", caseId);
		body.put("status_id", statusId);
		if (!(commentOrNull == null || commentOrNull.isBlank())) {
			body.put("comment", commentOrNull);
		}
		String json = jsonMapper.writeValueAsString(body);
		int timeoutSec = Math.max(1, properties.getRequestTimeoutSeconds());
		HttpRequest request = HttpRequest.newBuilder(uri)
				.timeout(Duration.ofSeconds(timeoutSec))
				.header("Authorization", auth)
				.header("Content-Type", "application/json; charset=UTF-8")
				.POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
				.build();

		log.debug("TestRail HTTP POST uri={} caseId={} statusId={}", uri, caseId, statusId);

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		int code = response.statusCode();
		if (code >= 200 && code < 300) {
			log.debug("TestRail HTTP {} OK (body omitted from logs)", code);
			return;
		}
		String preview = TestRailUriSupport.previewForLog(response.body(), BODY_PREVIEW_MAX);
		throw new TestRailIntegrationException("Non-success response from TestRail", code, preview);
	}

	private static String buildBasicAuthorization(String user, String apiKey) {
		String token = user + ":" + apiKey;
		String encoded = java.util.Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
		return "Basic " + encoded;
	}
}
