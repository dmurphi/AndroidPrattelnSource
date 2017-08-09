package com.cityparking.pratteln.webservices.entity;

public class ComputedTariffResponse {

	private Tariff tariff;

	public Tariff getTariff() {
		return tariff;
	}

    @SuppressWarnings("unused")
	public void setTariff(Tariff tariff) {
		this.tariff = tariff;
	}

	@Override
	public String toString() {
		return "ComputedTariffResponse [tariff=" + tariff + "]";
	}

}
