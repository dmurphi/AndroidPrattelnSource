package com.cityparking.pratteln.webservices.entity;

@SuppressWarnings("unused")
public class TimeIntervalsResponseBody {
	private String openInterval;
	private String startParkingHour;
	private String endParkingHour;
	private String currency;

	public String getOpenInterval() {
		return openInterval;
	}

	public void setOpenInterval(String openInterval) {
		this.openInterval = openInterval;
	}

	public String getStartParkingHour() {
		return startParkingHour;
	}

	public void setStartParkingHour(String startParkingHour) {
		this.startParkingHour = startParkingHour;
	}

	public String getEndParkingHour() {
		return endParkingHour;
	}

	public void setEndParkingHour(String endParkingHour) {
		this.endParkingHour = endParkingHour;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}
}
