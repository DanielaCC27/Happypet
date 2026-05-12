package com.uq.happypet.testrail;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "testrail")
public class TestRailProperties {

	private Boolean enabled;
	private String url;
	private String user;
	private String apiKey;
	private Long runId;
	private Integer statusPassedId = 1;
	private Integer statusFailedId = 5;
	private int requestTimeoutSeconds = 30;

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public Long getRunId() {
		return runId;
	}

	public void setRunId(Long runId) {
		this.runId = runId;
	}

	public Integer getStatusPassedId() {
		return statusPassedId;
	}

	public void setStatusPassedId(Integer statusPassedId) {
		this.statusPassedId = statusPassedId;
	}

	public Integer getStatusFailedId() {
		return statusFailedId;
	}

	public void setStatusFailedId(Integer statusFailedId) {
		this.statusFailedId = statusFailedId;
	}

	public int getRequestTimeoutSeconds() {
		return requestTimeoutSeconds;
	}

	public void setRequestTimeoutSeconds(int requestTimeoutSeconds) {
		this.requestTimeoutSeconds = requestTimeoutSeconds;
	}

	public boolean hasMinimumConfiguration() {
		return notBlank(url) && notBlank(user) && notBlank(apiKey) && runId != null && runId > 0;
	}

	public boolean isIntegrationEnabled() {
		if (Boolean.FALSE.equals(enabled)) {
			return false;
		}
		if (Boolean.TRUE.equals(enabled)) {
			return hasMinimumConfiguration();
		}
		return hasMinimumConfiguration();
	}

	private static boolean notBlank(String s) {
		return s != null && !s.isBlank();
	}
}
