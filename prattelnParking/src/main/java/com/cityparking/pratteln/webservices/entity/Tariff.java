package com.cityparking.pratteln.webservices.entity;

public class Tariff {

    private String computedValue;
    private boolean hasSpecialTimeLimit;
    private String timeLimit;
    private String parkingDurationMinutes;
    private String freeParkingDurationMinutes;
    private boolean whiteListCNP;

    public String getComputedValue() {
        return computedValue;
    }

    @SuppressWarnings("unused")
    public void setComputedValue(String computedValue) {
        this.computedValue = computedValue;
    }

    public boolean isHasSpecialTimeLimit() {
        return hasSpecialTimeLimit;
    }

    @SuppressWarnings("unused")
    public void setHasSpecialTimeLimit(boolean hasSpecialTimeLimit) {
        this.hasSpecialTimeLimit = hasSpecialTimeLimit;
    }

    public String getTimeLimit() {
        return timeLimit;
    }

    @SuppressWarnings("unused")
    public void setTimeLimit(String timeLimit) {
        this.timeLimit = timeLimit;
    }

    public String getParkingDurationMinutes() {
        return parkingDurationMinutes;
    }

    public void setParkingDurationMinutes(String parkingDurationMinutes) {
        this.parkingDurationMinutes = parkingDurationMinutes;
    }

    public String getFreeParkingDurationMinutes() {
        return freeParkingDurationMinutes;
    }

    public void setFreeParkingDurationMinutes(String freeParkingDurationMinutes) {
        this.freeParkingDurationMinutes = freeParkingDurationMinutes;
    }

    public boolean isWhiteListCNP() {
        return whiteListCNP;
    }

    public void setWhiteListCNP(boolean whiteListCNP) {
        this.whiteListCNP = whiteListCNP;
    }

    @Override
    public String toString() {
        return "Tariff{" +
                "computedValue='" + computedValue + '\'' +
                ", hasSpecialTimeLimit=" + hasSpecialTimeLimit +
                ", timeLimit='" + timeLimit + '\'' +
                ", parkingDurationMinutes='" + parkingDurationMinutes + '\'' +
                ", freeParkingDurationMinutes='" + freeParkingDurationMinutes + '\'' +
                ", whiteListCNP=" + whiteListCNP +
                '}';
    }
}
