package com.uq.happypet.testrail.junit;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Loads testrail-automated-mapping.properties: keys FQCN#methodName to TestRail case references (TC-*).
 */
final class TestRailAutomatedMappingLoader {

	private static final String RESOURCE = "testrail-automated-mapping.properties";

	private TestRailAutomatedMappingLoader() {
	}

	static Map<String, String> load() {
		try (InputStream in = TestRailAutomatedMappingLoader.class.getClassLoader().getResourceAsStream(RESOURCE)) {
			if (in == null) {
				return Collections.emptyMap();
			}
			Properties p = new Properties();
			p.load(in);
			Map<String, String> out = new LinkedHashMap<>();
			for (String name : p.stringPropertyNames()) {
				if (name.startsWith("#") || name.isBlank()) {
					continue;
				}
				String v = p.getProperty(name);
				if (v != null && !v.isBlank()) {
					out.put(name, v.strip());
				}
			}
			return Collections.unmodifiableMap(out);
		}
		catch (IOException e) {
			throw new IllegalStateException("Failed to load " + RESOURCE, e);
		}
	}
}