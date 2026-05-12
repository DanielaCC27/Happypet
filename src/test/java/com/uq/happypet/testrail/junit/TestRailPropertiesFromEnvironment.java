package com.uq.happypet.testrail.junit;

import com.uq.happypet.testrail.TestRailProperties;

/**
 * Builds TestRailProperties from environment variables (same names Spring Boot relaxed binding uses).
 */
final class TestRailPropertiesFromEnvironment {

	private TestRailPropertiesFromEnvironment() {
	}

	static TestRailProperties load() {
		TestRailProperties p = new TestRailProperties();
		p.setUrl(firstNonBlank(env("TESTRAIL_URL"), env("TESTRAIL_BASE_URL")));
		p.setUser(env("TESTRAIL_USER"));
		p.setApiKey(env("TESTRAIL_API_KEY"));
		p.setRunId(parseLong(env("TESTRAIL_RUN_ID")));
		p.setEnabled(parseBoolean(env("TESTRAIL_ENABLED")));
		p.setStatusPassedId(parseInteger(env("TESTRAIL_STATUS_PASSED_ID")));
		p.setStatusFailedId(parseInteger(env("TESTRAIL_STATUS_FAILED_ID")));
		String to = env("TESTRAIL_REQUEST_TIMEOUT_SECONDS");
		if (to != null && !to.isBlank()) {
			try {
				p.setRequestTimeoutSeconds(Integer.parseInt(to.strip()));
			}
			catch (NumberFormatException ignored) {
			}
		}
		return p;
	}

	private static String env(String key) {
		String v = System.getenv(key);
		return v != null ? v.strip() : null;
	}

	private static String firstNonBlank(String a, String b) {
		if (a != null && !a.isBlank()) {
			return a;
		}
		if (b != null && !b.isBlank()) {
			return b;
		}
		return null;
	}

	private static Long parseLong(String s) {
		if (s == null || s.isBlank()) {
			return null;
		}
		try {
			return Long.parseLong(s.strip());
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	private static Integer parseInteger(String s) {
		if (s == null || s.isBlank()) {
			return null;
		}
		try {
			return Integer.parseInt(s.strip());
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	private static Boolean parseBoolean(String s) {
		if (s == null || s.isBlank()) {
			return null;
		}
		return Boolean.parseBoolean(s.strip());
	}
}