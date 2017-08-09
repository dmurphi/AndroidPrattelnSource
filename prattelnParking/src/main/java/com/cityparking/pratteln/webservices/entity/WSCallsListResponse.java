package com.cityparking.pratteln.webservices.entity;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class WSCallsListResponse<T> {
	private ArrayList<T> responseBody = new ArrayList<T>();
	private String errorMessage;
	private Boolean succeeded;

	public ArrayList<T> getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(ArrayList<T> responseBody) {
		this.responseBody = responseBody;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public Boolean getSucceeded() {
		return succeeded;
	}

	public void setSucceeded(Boolean succeeded) {
		this.succeeded = succeeded;
	}

	@Override
	public String toString() {
		return "WSCallsListResponse [responseBody=" + responseBody + ", errorMessage=" + errorMessage + ", succeeded=" + succeeded + "]";
	}

}
