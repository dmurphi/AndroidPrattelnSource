package com.cityparking.pratteln.entities;

public class Car {

    String cnp;
    long exactTime = -1;
    long timeToCount = -1;
    boolean parked = false;
    int parkedLocationId = -1;

    @Override
    public String toString() {
        return "Car{" +
                "cnp='" + cnp + '\'' +
                ", exactTime=" + exactTime +
                ", timeToCount=" + timeToCount +
                ", parked=" + parked +
                ", parkedLocationId='" + parkedLocationId + '\'' +
                '}';
    }

    public Car(String cnp) {
        super();
        this.cnp = cnp;
    }

    public Car(String cnp, long exactTime, int timeToCount, boolean parked) {
        super();
        this.cnp = cnp;
        this.exactTime = exactTime;
        this.timeToCount = timeToCount;
        this.parked = parked;
    }

    public String getCnp() {
        return cnp;
    }

    public void setCnp(String cnp) {
        this.cnp = cnp;
    }

    public long getExactTime() {
        return exactTime;
    }

    public void setExactTime(long exactTime) {
        this.exactTime = exactTime;
    }

    public long getTimeToCount() {
        return timeToCount;
    }

    public void setTimeToCount(long timeToCount) {
        this.timeToCount = timeToCount;
    }

    public boolean isParked() {
        return parked;
    }

    public void setParked(boolean parked) {
        this.parked = parked;
    }

    public int getParkedLocationId() {
        return parkedLocationId;
    }

    public void setParkedLocationId(int parkedLocationId) {
        this.parkedLocationId = parkedLocationId;
    }
}
