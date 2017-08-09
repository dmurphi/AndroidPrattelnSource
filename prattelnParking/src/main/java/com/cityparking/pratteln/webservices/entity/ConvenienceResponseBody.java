package com.cityparking.pratteln.webservices.entity;

public class ConvenienceResponseBody {

	private String ConvenienceFeeAmount;

	public String getConvenienceFeeAmount() {
		return ConvenienceFeeAmount;
	}

    @SuppressWarnings("unused")
	public void setConvenienceFeeAmount(String convenienceFeeAmount) {
		this.ConvenienceFeeAmount = convenienceFeeAmount;
	}

	@Override
	public String toString() {
		return "ConvenienceResponseBody [convenienceFeeAmount=" + ConvenienceFeeAmount + "]";
	}

}
