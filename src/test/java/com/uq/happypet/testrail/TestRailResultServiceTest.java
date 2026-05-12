package com.uq.happypet.testrail;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TestRailResultServiceTest {

	@Mock
	private TestRailRestClient restClient;

	private final TestRailProperties properties = new TestRailProperties();

	private TestRailResultService service;

	@BeforeEach
	void setUp() {
		service = new TestRailResultService(properties, restClient);
	}

	@Test
	void whenNotConfigured_doesNotCallClient() throws Exception {
		service.sendPassed(100);
		service.sendFailed(100, "error");
		verifyNoInteractions(restClient);
	}

	@Test
	void whenConfigured_sendsPassed() throws Exception {
		properties.setUrl("https://demo.testrail.io");
		properties.setUser("mail@test.com");
		properties.setApiKey("key");
		properties.setRunId(7L);

		service.sendPassed(99);

		verify(restClient).addResult(7L, 99L, 1, null);
	}

	@Test
	void whenConfigured_sendsFailedWithComment() throws Exception {
		properties.setUrl("https://demo.testrail.io");
		properties.setUser("mail@test.com");
		properties.setApiKey("key");
		properties.setRunId(7L);

		service.sendFailed(3, "assertion failed");

		verify(restClient).addResult(7L, 3L, 5, "assertion failed");
	}

	@Test
	void whenEnabledFalse_doesNotCallClient() throws Exception {
		properties.setEnabled(false);
		properties.setUrl("https://demo.testrail.io");
		properties.setUser("mail@test.com");
		properties.setApiKey("key");
		properties.setRunId(7L);

		service.sendPassed(1);

		verifyNoInteractions(restClient);
	}

	@Test
	void httpError_propagates() throws Exception {
		properties.setUrl("https://demo.testrail.io");
		properties.setUser("mail@test.com");
		properties.setApiKey("key");
		properties.setRunId(7L);
		doThrow(new TestRailIntegrationException("fail", 401, "unauthorized"))
				.when(restClient).addResult(7L, 1L, 1, null);

		assertThrows(TestRailIntegrationException.class, () -> service.sendPassed(1));
	}

	@Test
	void ioError_wraps() throws Exception {
		properties.setUrl("https://demo.testrail.io");
		properties.setUser("mail@test.com");
		properties.setApiKey("key");
		properties.setRunId(7L);
		doThrow(new IOException("network")).when(restClient).addResult(7L, 1L, 1, null);

		assertThrows(TestRailIntegrationException.class, () -> service.sendPassed(1));
	}
}
