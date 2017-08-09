package com.cityparking.pratteln.webservices.entity;

@SuppressWarnings("unused")
public class CallParkingChargesResponseBody {
	private Double rate;
	private Double minutes;

	public Double getRate() {
		return rate;
	}

	public void setRate(Double rate) {
		this.rate = rate;
	}

	public Double getMinutes() {
		return minutes;
	}

	public void setMinutes(Double minutes) {
		this.minutes = minutes;
	}

	@Override
	public String toString() {
		return "CallParkingChargesResponseBody [rate=" + rate + ", minutes=" + minutes + "]";
	}

}
