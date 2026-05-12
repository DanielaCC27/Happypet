package com.uq.happypet.testrail;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TestRailResultService {

	private static final Logger log = LoggerFactory.getLogger(TestRailResultService.class);

	private final TestRailProperties properties;
	private final TestRailRestClient restClient;
	private final AtomicBoolean missingConfigWarned = new AtomicBoolean(false);

	public TestRailResultService(TestRailProperties properties, TestRailRestClient restClient) {
		this.properties = properties;
		this.restClient = restClient;
	}

	public void sendPassed(long caseId) {
		sendResult(caseId, resolvePassedStatusId(), null, "Passed");
	}

	public void sendFailed(long caseId, String comment) {
		sendResult(caseId, resolveFailedStatusId(), comment, "Failed");
	}

	private int resolvePassedStatusId() {
		Integer id = properties.getStatusPassedId();
		return id != null ? id : 1;
	}

	private int resolveFailedStatusId() {
		Integer id = properties.getStatusFailedId();
		return id != null ? id : 5;
	}

	private void sendResult(long caseId, int statusId, String comment, String label) {
		if (Boolean.TRUE.equals(properties.getEnabled()) && !properties.hasMinimumConfiguration()) {
			if (missingConfigWarned.compareAndSet(false, true)) {
				log.warn("TestRail: testrail.enabled=true but url, user, api-key or run-id is missing; results will not be sent");
			}
		}
		if (!properties.isIntegrationEnabled()) {
			log.debug("TestRail: integration inactive; skip {} for caseId={}", label, caseId);
			return;
		}
		Long runId = properties.getRunId();
		try {
			restClient.addResult(runId, caseId, statusId, comment);
			log.info("TestRail: {} recorded runId={} caseId={} statusId={}", label, runId, caseId, statusId);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new TestRailIntegrationException("Interrupted while sending result to TestRail", e);
		}
		catch (IOException e) {
			throw new TestRailIntegrationException("Network or I/O error while sending result to TestRail", e);
		}
		catch (TestRailIntegrationException e) {
			log.error("TestRail: HTTP failure sending {} runId={} caseId={}: {}", label, runId, caseId, e.getMessage());
			throw e;
		}
	}
}
