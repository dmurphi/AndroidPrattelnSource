package com.cityparking.pratteln.webservices.entity;

public class ResidentTariff {

    private Integer locationId;
    private String locationName;
    private Boolean hasFlatRate;
    private Double flatRateForToday;
    private Integer maxParkingHours;
    private Integer step;
    private ParkingData parkingData;
    private SpotsResponseBody parkingSpots;

    /**
     * @return The locationId
     */
    @SuppressWarnings("unused")
    public Integer getLocationId() {
        return locationId;
    }

    /**
     * @param locationId The locationId
     */
    @SuppressWarnings("unused")
    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }

    /**
     * @return The locationName
     */
    @SuppressWarnings("unused")
    public String getLocationName() {
        return locationName;
    }

    /**
     * @param locationName The locationName
     */
    @SuppressWarnings("unused")
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    /**
     * @return The hasFlatRate
     */
    public Boolean getHasFlatRate() {
        return hasFlatRate;
    }

    /**
     * @param hasFlatRate The hasFlatRate
     */
    @SuppressWarnings("unused")
    public void setHasFlatRate(Boolean hasFlatRate) {
        this.hasFlatRate = hasFlatRate;
    }

    /**
     * @return The flatRateForToday
     */
    public Double getFlatRateForToday() {
        return flatRateForToday;
    }

    /**
     * @param flatRateForToday The flatRateForToday
     */
    @SuppressWarnings("unused")
    public void setFlatRateForToday(Double flatRateForToday) {
        this.flatRateForToday = flatRateForToday;
    }

    /**
     * @return The maxParkingHours
     */
    public Integer getMaxParkingHours() {
        return maxParkingHours;
    }

    /**
     * @param maxParkingHours The maxParkingHours
     */
    @SuppressWarnings("unused")
    public void setMaxParkingHours(Integer maxParkingHours) {
        this.maxParkingHours = maxParkingHours;
    }

    /**
     * @return The step
     */
    public Integer getStep() {
        return step;
    }

    /**
     * @param step The step
     */
    @SuppressWarnings("unused")
    public void setStep(Integer step) {
        this.step = step;
    }

    /**
     * @return The parkingData
     */
    public ParkingData getParkingData() {
        return parkingData;
    }

    /**
     * @param parkingData The parkingData
     */
    @SuppressWarnings("unused")
    public void setParkingData(ParkingData parkingData) {
        this.parkingData = parkingData;
    }

    /**
     * @return The parkingSpots
     */
    @SuppressWarnings("unused")
    public SpotsResponseBody getParkingSpots() {
        return parkingSpots;
    }

    /**
     * @param parkingSpots The parkingSpots
     */
    @SuppressWarnings("unused")
    public void setParkingSpots(SpotsResponseBody parkingSpots) {
        this.parkingSpots = parkingSpots;
    }

}
