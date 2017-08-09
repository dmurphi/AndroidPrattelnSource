package com.cityparking.pratteln.entities;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Reea164 on 6/21/2016.
 */
public class LastParking {

    private String cnp;
    private long currentTimeInMillis;
    private String lastParkingCost;
    private String lastParkingDuration;
    private int maxParkingHours;
    private int locationAreaId;

    public LastParking() {
    }

    public LastParking(String cnp, long timeStamp, String lastParkingDuration, String lastParkingCost, int maxParkingHours, int locationAreaId) {
        this.cnp = cnp;
        this.currentTimeInMillis = timeStamp;
        this.lastParkingDuration = lastParkingDuration;
        this.lastParkingCost = lastParkingCost;
        this.maxParkingHours = maxParkingHours;
        this.locationAreaId = locationAreaId;
    }

    public String getCnp() {
        return cnp;
    }

    public void setCnp(String cnp) {
        this.cnp = cnp;
    }

    public long getTimeStamp() {
        return currentTimeInMillis;
    }

    public void setTimeStamp(long timeStamp) {
        this.currentTimeInMillis = timeStamp;
    }

    public String getLastParkingCost() {
        return lastParkingCost;
    }

    public void setLastParkingCost(String lastParkingCost) {
        this.lastParkingCost = lastParkingCost;
    }

    public String getLastParkingDuration() throws NullPointerException{
        return lastParkingDuration;
    }

    public void setLastParkingDuration(String lastParkingDuration) {
        this.lastParkingDuration = lastParkingDuration;
    }

    public Integer getMaxParkingHours() {
        return maxParkingHours;
    }

    public void setMaxParkingHours(Integer maxParkingHours) {
        this.maxParkingHours = maxParkingHours;
    }

    public long getCurrentTimeInMillis() {
        return currentTimeInMillis;
    }

    public void setCurrentTimeInMillis(long currentTimeInMillis) {
        this.currentTimeInMillis = currentTimeInMillis;
    }

    public void setMaxParkingHours(int maxParkingHours) {
        this.maxParkingHours = maxParkingHours;
    }

    public int getLocationAreaId() {
        return locationAreaId;
    }

    public void setLocationAreaId(int locationAreaId) {
        this.locationAreaId = locationAreaId;
    }

    public JSONObject getJSONObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("plate_number", this.cnp);
            obj.put("current_time_in_millis", this.currentTimeInMillis);
            obj.put("parking_duration", this.lastParkingDuration);
            obj.put("parking_cost", this.lastParkingCost);
            obj.put("max_parking_hour", this.maxParkingHours);
            obj.put("location_area_id", this.locationAreaId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    @Override
    public String toString() {
        return "LastParking{" +
                "cnp='" + cnp + '\'' +
                ", timeStamp=" + currentTimeInMillis +
                ", pastParkingCost='" + lastParkingCost + '\'' +
                ", lastParkingDuration='" + lastParkingDuration + '\'' +
                '}';
    }
}
