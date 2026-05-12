package com.uq.happypet.testrail;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.junit.jupiter.api.Test;

class TestRailUriSupportTest {

	@Test
	void normalizesHostWithoutApiV2() {
		assertThat(TestRailUriSupport.normalizeApiBase("https://company.testrail.io/"))
				.isEqualTo("https://company.testrail.io/index.php?/api/v2");
	}

	@Test
	void keepsUrlThatAlreadyIncludesApiV2() {
		String base = "https://company.testrail.io/index.php?/api/v2";
		assertThat(TestRailUriSupport.normalizeApiBase(base + "/")).isEqualTo(base);
	}

	@Test
	void addResultUriEndsWithRunId() {
		URI uri = TestRailUriSupport.addResultUri("https://company.testrail.io", 42L);
		assertThat(uri.toString()).endsWith("/add_result/42");
		assertThat(uri.toString()).contains("index.php?/api/v2");
	}
}
