package com.uq.happypet.testrail.junit;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Properties;

/**
 * Loads testrail-case-ids.properties: TestRail case reference (TC-*) to numeric case_id for the API.
 */
final class TestRailCaseNumericIdLoader {

	private static final String RESOURCE = "testrail-case-ids.properties";

	private TestRailCaseNumericIdLoader() {
	}

	static Map<String, Long> load() {
		try (InputStream in = TestRailCaseNumericIdLoader.class.getClassLoader().getResourceAsStream(RESOURCE)) {
			if (in == null) {
				return Collections.emptyMap();
			}
			Properties p = new Properties();
			p.load(in);
			Map<String, Long> out = new LinkedHashMap<>();
			for (String name : p.stringPropertyNames()) {
				if (name.startsWith("#") || name.isBlank()) {
					continue;
				}
				String raw = p.getProperty(name);
				if (raw == null || raw.isBlank()) {
					continue;
				}
				String key = name.strip();
				String num = raw.strip();
				try {
					out.put(key, Long.parseLong(num));
				}
				catch (NumberFormatException ignored) {
				}
			}
			return Collections.unmodifiableMap(out);
		}
		catch (IOException e) {
			throw new IllegalStateException("Failed to load " + RESOURCE, e);
		}
	}

	static OptionalLong resolveNumericId(Map<String, Long> idsByRef, String caseRef) {
		Long id = idsByRef.get(caseRef);
		if (id == null || id <= 0) {
			return OptionalLong.empty();
		}
		return OptionalLong.of(id);
	}
}