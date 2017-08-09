package com.cityparking.pratteln.webservices.entity;

public class LocationParkingDatum {

	private Integer locationId;
	private String locationName;
	private Boolean hasFlatRate;
	private Boolean isDayOnly;
	private Double flatRateForToday;
	private Integer maxParkingHours;
	private Integer step;
	private ParkingData parkingData;
	private SpotsResponseBody parkingSpots;

	/**
	 * 
	 * @return The locationId
	 */
	public Integer getLocationId() {
		return locationId;
	}

	/**
	 * 
	 * @param locationId
	 *            The locationId
	 */
	public void setLocationId(Integer locationId) {
		this.locationId = locationId;
	}

	/**
	 * 
	 * @return The locationName
	 */
	public String getLocationName() {
		return locationName;
	}

	/**
	 * 
	 * @param locationName
	 *            The locationName
	 */

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	/**
	 * 
	 * @return The hasFlatRate
	 */
	public Boolean getHasFlatRate() {
		return hasFlatRate;
	}

	/**
	 * 
	 * @param hasFlatRate
	 *            The hasFlatRate
	 */
	public void setHasFlatRate(Boolean hasFlatRate) {
		this.hasFlatRate = hasFlatRate;
	}

	/**
	 * 
	 * @return isDayOnly
	 */
	public Boolean getIsDayOnly() {
		return isDayOnly;
	}

	/**
	 * 
	 * @param isDayOnly
	 */
	public void setIsDayOnly(Boolean isDayOnly) {
		this.isDayOnly = isDayOnly;
	}

	/**
	 * 
	 * @return The flatRateForToday
	 */
	public Double getFlatRateForToday() {
		return flatRateForToday;
	}

	/**
	 * 
	 * @param flatRateForToday
	 *            The flatRateForToday
	 */
	public void setFlatRateForToday(Double flatRateForToday) {
		this.flatRateForToday = flatRateForToday;
	}

	/**
	 * 
	 * @return The step
	 */
	public Integer getStep() {
		return step;
	}

	/**
	 * 
	 * @param step
	 *            The step
	 */
	public void setStep(Integer step) {
		this.step = step;
	}

	/**
	 * 
	 * @return The parkingData
	 */
	public ParkingData getParkingData() {
		return parkingData;
	}

	/**
	 * 
	 * @param parkingData
	 *            The parkingData
	 */
	public void setParkingData(ParkingData parkingData) {
		this.parkingData = parkingData;
	}

	/**
	 * 
	 * @return The parkingSpots
	 */
	public SpotsResponseBody getParkingSpots() {
		return parkingSpots;
	}

	/**
	 * 
	 * @param parkingSpots
	 *            The parkingSpots
	 */
	public void setParkingSpots(SpotsResponseBody parkingSpots) {
		this.parkingSpots = parkingSpots;
	}

	/**
	 * 
	 * @return The maxParkingHours
	 */
	public Integer getMaxParkingHours() {
		return maxParkingHours;
	}

	/**
	 * 
	 * @param maxParkingHours
	 *            The maxParkinHours
	 */
	public void setMaxParkingHours(Integer maxParkingHours) {
		this.maxParkingHours = maxParkingHours;
	}

}
