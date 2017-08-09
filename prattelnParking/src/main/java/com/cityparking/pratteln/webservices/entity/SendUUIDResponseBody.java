package com.cityparking.pratteln.webservices.entity;

public class SendUUIDResponseBody {
	private Object RefCardNo;
	private Boolean transactionReady;
	private String transactionId;

	public Object getRefCardNo() {
		return RefCardNo;
	}

	public void setRefCardNo(Object RefCardNo) {
		this.RefCardNo = RefCardNo;
	}

	public Boolean getTransactionReady() {
		return transactionReady;
	}

	public void setTransactionReady(Boolean transactionReady) {
		this.transactionReady = transactionReady;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
}
