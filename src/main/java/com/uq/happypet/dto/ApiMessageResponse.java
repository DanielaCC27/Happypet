package com.uq.happypet.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiMessageResponse {

    private String message;
    private String error;
    private Map<String, String> fieldErrors;

    public ApiMessageResponse() {}

    public ApiMessageResponse(String message) {
        this.message = message;
    }

    public ApiMessageResponse(String error, String message) {
        this.error = error;
        this.message = message;
    }

    public ApiMessageResponse(String error, String message, Map<String, String> fieldErrors) {
        this.error = error;
        this.message = message;
        this.fieldErrors = fieldErrors;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
}