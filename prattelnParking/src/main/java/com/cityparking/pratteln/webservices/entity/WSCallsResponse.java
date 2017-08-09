package com.cityparking.pratteln.webservices.entity;

import com.google.gson.annotations.Expose;

public class WSCallsResponse<T> {

	private Boolean succeeded;
	@Expose
	private String errorMessage;
	@Expose
	private T responseBody;

    @SuppressWarnings("unused")
	public Boolean getSucceeded() {
		return succeeded;
	}

    @SuppressWarnings("unused")
	public void setSucceeded(Boolean succeeded) {
		this.succeeded = succeeded;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

    @SuppressWarnings("unused")
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public T getResponseBody() {
		return responseBody;
	}

    @SuppressWarnings("unused")
	public void setResponseBody(T responseBody) {
		this.responseBody = responseBody;
	}

	@Override
	public String toString() {
		return "WSCallsResponse [succeeded=" + succeeded + ", errorMessage=" + errorMessage + ", responseBody=" + responseBody + "]";
	}

}
