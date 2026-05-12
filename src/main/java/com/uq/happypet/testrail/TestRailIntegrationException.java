package com.uq.happypet.testrail;

public class TestRailIntegrationException extends RuntimeException {

	private final Integer httpStatus;

	public TestRailIntegrationException(String message) {
		super(message);
		this.httpStatus = null;
	}

	public TestRailIntegrationException(String message, Throwable cause) {
		super(message, cause);
		this.httpStatus = null;
	}

	public TestRailIntegrationException(String message, int httpStatus, String responseBodyPreview) {
		super(buildDetail(message, httpStatus, responseBodyPreview));
		this.httpStatus = httpStatus;
	}

	public Integer getHttpStatus() {
		return httpStatus;
	}

	private static String buildDetail(String message, int httpStatus, String responseBodyPreview) {
		StringBuilder sb = new StringBuilder(message);
		sb.append(" (HTTP ").append(httpStatus).append(")");
		if (responseBodyPreview != null && !responseBodyPreview.isBlank()) {
			sb.append(": ").append(responseBodyPreview);
		}
		return sb.toString();
	}
}
