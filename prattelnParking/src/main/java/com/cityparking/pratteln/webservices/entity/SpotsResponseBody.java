package com.cityparking.pratteln.webservices.entity;

import java.util.ArrayList;
import java.util.List;

public class SpotsResponseBody {

    private List<Integer> ValidSpotIds = new ArrayList<Integer>();
    private Integer NumberOfParkingSpots;

    @SuppressWarnings("unused")
    public List<Integer> getValidSpotIds() {
        return ValidSpotIds;
    }

    @SuppressWarnings("unused")
    public void setValidSpotIds(List<Integer> validSpotIds) {
        this.ValidSpotIds = validSpotIds;
    }

    @SuppressWarnings("unused")
    public Integer getNumberOfParkingSpots() {
        return NumberOfParkingSpots;
    }

    @SuppressWarnings("unused")
    public void setNumberOfParkingSpots(Integer numberOfParkingSpots) {
        this.NumberOfParkingSpots = numberOfParkingSpots;
    }
}
