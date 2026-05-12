package com.uq.happypet.testrail.junit;

import java.util.Map;
import java.util.OptionalLong;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uq.happypet.testrail.TestRailIntegrationException;
import com.uq.happypet.testrail.TestRailProperties;
import com.uq.happypet.testrail.TestRailRestClient;
import com.uq.happypet.testrail.TestRailResultService;

import tools.jackson.databind.json.JsonMapper;

/**
 * After each mapped test, optionally reports pass/fail to TestRail.
 * Mapping: testrail-automated-mapping.properties (FQCN#method to TC-*).
 * Numeric case id: testrail-case-ids.properties (TC-* to number).
 * Requires TESTRAIL_REPORT_RESULTS=true and valid TestRail env.
 * Never fails the JUnit outcome if TestRail is unreachable.
 */
public class TestRailJUnitExtension implements AfterTestExecutionCallback {

	private static final Logger log = LoggerFactory.getLogger(TestRailJUnitExtension.class);
	private static final int COMMENT_MAX = 2000;

	private static final Map<String, String> MAPPING = TestRailAutomatedMappingLoader.load();
	private static final Map<String, Long> CASE_IDS = TestRailCaseNumericIdLoader.load();

	@Override
	public void afterTestExecution(ExtensionContext context) {
		if (!reportingSwitchOn()) {
			return;
		}
		Class<?> testClass = context.getRequiredTestClass();
		if (testClass.getName().startsWith("com.uq.happypet.testrail.")) {
			return;
		}
		String fqcn = context.getRequiredTestMethod().getDeclaringClass().getName();
		String method = context.getRequiredTestMethod().getName();
		String key = fqcn + "#" + method;
		String caseRef = MAPPING.get(key);
		if (caseRef == null) {
			log.trace("TestRail: no mapping entry for {}", key);
			return;
		}
		OptionalLong numeric = TestRailCaseNumericIdLoader.resolveNumericId(CASE_IDS, caseRef);
		if (numeric.isEmpty()) {
			log.debug("TestRail: no numeric case_id for ref {} key {}", caseRef, key);
			return;
		}
		TestRailProperties properties = TestRailPropertiesFromEnvironment.load();
		if (!properties.isIntegrationEnabled()) {
			log.debug("TestRail: integration inactive; skip key {}", key);
			return;
		}
		long caseId = numeric.getAsLong();
		TestRailResultService service = new TestRailResultService(properties,
				new TestRailRestClient(properties, JsonMapper.builder().build()));
		try {
			if (context.getExecutionException().isPresent()) {
				Throwable t = context.getExecutionException().get();
				String msg = t.getMessage() != null ? t.getMessage() : t.getClass().getName();
				service.sendFailed(caseId, truncate(msg, COMMENT_MAX));
			}
			else {
				service.sendPassed(caseId);
			}
		}
		catch (TestRailIntegrationException e) {
			log.warn("TestRail: could not report result for {} ref={} caseId={}: {}", key, caseRef, caseId, e.getMessage());
		}
		catch (Exception e) {
			log.warn("TestRail: unexpected error reporting {} ref={} caseId={}: {}", key, caseRef, caseId, e.toString());
		}
	}

	private static boolean reportingSwitchOn() {
		String v = System.getenv("TESTRAIL_REPORT_RESULTS");
		if (v == null || v.isBlank()) {
			v = System.getProperty("testrail.report.results");
		}
		return "true".equalsIgnoreCase(v != null ? v.strip() : null);
	}

	private static String truncate(String s, int max) {
		if (s.length() <= max) {
			return s;
		}
		return s.substring(0, max) + "...";
	}
}