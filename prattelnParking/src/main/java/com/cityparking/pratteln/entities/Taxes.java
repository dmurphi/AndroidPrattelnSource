package com.cityparking.pratteln.entities;


public class Taxes {

	Double price;
	Double minutes;

	public Taxes(Double price, Double minutes) {
		super();
		this.price = price;
		this.minutes = minutes;
	}

    public Double getPrice() {
		return price;
	}

    @SuppressWarnings("unused")
	public void setPrice(Double price) {
		this.price = price;
	}

	public Double getMinutes() {
		return minutes;
	}

    @SuppressWarnings("unused")
	public void setMinutes(Double minutes) {
		this.minutes = minutes;
	}

}
