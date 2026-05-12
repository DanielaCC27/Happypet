package com.uq.happypet.testrail;

import java.net.URI;

final class TestRailUriSupport {

	private static final String API_V2_SUFFIX = "/index.php?/api/v2";

	private TestRailUriSupport() {
	}

	static String normalizeApiBase(String rawBaseUrl) {
		if (rawBaseUrl == null || rawBaseUrl.isBlank()) {
			return "";
		}
		String trimmed = rawBaseUrl.trim();
		while (trimmed.endsWith("/")) {
			trimmed = trimmed.substring(0, trimmed.length() - 1);
		}
		if (trimmed.contains("/index.php?/api/v2")) {
			int idx = trimmed.indexOf("/index.php?/api/v2");
			return trimmed.substring(0, idx + API_V2_SUFFIX.length());
		}
		return trimmed + API_V2_SUFFIX;
	}

	static URI addResultUri(String apiBase, long runId) {
		String base = normalizeApiBase(apiBase);
		String path = base + "/add_result/" + runId;
		return URI.create(path);
	}

	static String previewForLog(String text, int maxLen) {
		if (text == null) {
			return "";
		}
		String t = text.replace("\r", " ").replace("\n", " ").strip();
		if (t.length() <= maxLen) {
			return t;
		}
		return t.substring(0, maxLen) + "...";
	}
}
