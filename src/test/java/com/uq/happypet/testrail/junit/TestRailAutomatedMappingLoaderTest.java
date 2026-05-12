package com.uq.happypet.testrail.junit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TestRailAutomatedMappingLoaderTest {

	@Test
	void loadsAuthApiMapping() {
		assertThat(TestRailAutomatedMappingLoader.load())
				.containsEntry(
						"com.uq.happypet.api.AuthApiControllerTest#register_exitoso_retornaCreated",
						"TC-AUTH-001");
	}
}