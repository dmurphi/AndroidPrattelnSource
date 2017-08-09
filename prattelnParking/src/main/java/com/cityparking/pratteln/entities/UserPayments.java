package com.cityparking.pratteln.entities;

public class UserPayments {

	public UserPayments(Integer paymentId, String carNumberPlate, String startTime, String amount) {
		super();
		this.carNumberPlate = carNumberPlate;
		this.amount = amount;
		this.startTime = startTime;
		this.paymentId = paymentId;
		this.checked = false;
		this.locationAreaId = 0;
		this.isFreeParking = false;
	}

	private String carNumberPlate;
	private String amount;
	private String startTime;
	private Integer paymentId;
	private Boolean isCnp;
	private Boolean checked = false;
	private Integer locationAreaId;
	private Boolean isFreeParking;

	public String getCarNumberPlate() {
		return carNumberPlate;
	}

    @SuppressWarnings("unused")
	public void setCarNumberPlate(String carNumberPlate) {
		this.carNumberPlate = carNumberPlate;
	}

	public String getAmount() {
		return amount;
	}

    @SuppressWarnings("unused")
	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getStartTime() {
		return startTime;
	}

    @SuppressWarnings("unused")
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public Integer getPaymentId() {
		return paymentId;
	}

    @SuppressWarnings("unused")
	public void setPaymentId(Integer paymentId) {
		this.paymentId = paymentId;
	}

    @SuppressWarnings("unused")
	public Boolean getIsCnp() {
		return isCnp;
	}

    @SuppressWarnings("unused")
	public void setIsCnp(Boolean isCnp) {
		this.isCnp = isCnp;
	}

	public Boolean isChecked() {
		return checked;
	}

	public void setIsChecked(Boolean isChecked) {
		this.checked = isChecked;
	}

	public Integer getLocationAreaId() {
		return locationAreaId;
	}

    @SuppressWarnings("unused")
	public void setLocationAreaId(Integer locationAreaId) {
		this.locationAreaId = locationAreaId;
	}

	public Boolean getIsFreeParking() {
		return isFreeParking;
	}

    @SuppressWarnings("unused")
	public void setIsFreeParking(Boolean isFreeParking) {
		this.isFreeParking = isFreeParking;
	}

	@Override
	public String toString() {
		return "UserPayments [carNumberPlate=" + carNumberPlate + ", amount=" + amount + ", startTime=" + startTime + ", paymentId=" + paymentId + ", isCnp=" + isCnp + ", checked=" + checked + ", locationAreaId=" + locationAreaId + ", isFreeParking="+ isFreeParking +"]";
	}

}