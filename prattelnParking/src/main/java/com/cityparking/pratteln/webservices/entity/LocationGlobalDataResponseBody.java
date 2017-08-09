package com.cityparking.pratteln.webservices.entity;

import java.util.ArrayList;
import java.util.List;

public class LocationGlobalDataResponseBody {

    private VATResponseBody vat;
    private ConvenienceResponseBody convenienceFee;
    private String currency;
    private String currencyISOCode;
    private String localHour;
    private String localMinutes;
    private Integer timeStamp;
    private List<LocationParkingDatum> locationParkingData = new ArrayList<LocationParkingDatum>();

    /**
     * @return The vat
     */
    public VATResponseBody getVat() {
        return vat;
    }

    /**
     * @param vat The vat
     */
    @SuppressWarnings("unused")
    public void setVat(VATResponseBody vat) {
        this.vat = vat;
    }

    /**
     * @return The convenienceFee
     */
    public ConvenienceResponseBody getConvenienceFee() {
        return convenienceFee;
    }

    /**
     * @param convenienceFee The convenienceFee
     */
    @SuppressWarnings("unused")
    public void setConvenienceFee(ConvenienceResponseBody convenienceFee) {
        this.convenienceFee = convenienceFee;
    }

    /**
     * @return The currency
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * @param currency The currency
     */
    @SuppressWarnings("unused")
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * @return The currencyISOCode
     */
    @SuppressWarnings("unused")
    public String getCurrencyISOCode() {
        return currencyISOCode;
    }

    /**
     * @param currencyISOCode The currencyISOCode
     */
    @SuppressWarnings("unused")
    public void setCurrencyISOCode(String currencyISOCode) {
        this.currencyISOCode = currencyISOCode;
    }

    /**
     * @return The localHour
     */
    @SuppressWarnings("unused")
    public String getLocalHour() {
        return localHour;
    }

    /**
     * @param localHour The localHour
     */
    @SuppressWarnings("unused")
    public void setLocalHour(String localHour) {
        this.localHour = localHour;
    }

    /**
     * @return The localMinutes
     */
    @SuppressWarnings("unused")
    public String getLocalMinutes() {
        return localMinutes;
    }

    /**
     * @param localMinutes The localMinutes
     */
    @SuppressWarnings("unused")
    public void setLocalMinutes(String localMinutes) {
        this.localMinutes = localMinutes;
    }

    /**
     * @return The timeStamp
     */
    @SuppressWarnings("unused")
    public Integer getTimeStamp() {
        return timeStamp;
    }

    /**
     * @param timeStamp The timeStamp
     */
    @SuppressWarnings("unused")
    public void setTimeStamp(Integer timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * @return The locationParkingData
     */
    public List<LocationParkingDatum> getLocationParkingData() {
        return locationParkingData;
    }

    /**
     * @param locationParkingData The locationParkingData
     */
    @SuppressWarnings("unused")
    public void setLocationParkingData(List<LocationParkingDatum> locationParkingData) {
        this.locationParkingData = locationParkingData;
    }

}
