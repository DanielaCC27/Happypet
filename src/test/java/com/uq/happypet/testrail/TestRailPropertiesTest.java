package com.uq.happypet.testrail;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TestRailPropertiesTest {

	@Test
	void noCredentials_integrationOff() {
		TestRailProperties p = new TestRailProperties();
		assertThat(p.isIntegrationEnabled()).isFalse();
	}

	@Test
	void minimumCredentials_integrationOn() {
		TestRailProperties p = new TestRailProperties();
		p.setUrl("https://x.testrail.io");
		p.setUser("u");
		p.setApiKey("k");
		p.setRunId(1L);
		assertThat(p.isIntegrationEnabled()).isTrue();
	}

	@Test
	void enabledFalse_forcesOff() {
		TestRailProperties p = new TestRailProperties();
		p.setEnabled(false);
		p.setUrl("https://x.testrail.io");
		p.setUser("u");
		p.setApiKey("k");
		p.setRunId(1L);
		assertThat(p.isIntegrationEnabled()).isFalse();
	}
}
