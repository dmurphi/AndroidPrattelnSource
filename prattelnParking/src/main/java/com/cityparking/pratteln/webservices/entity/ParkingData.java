package com.cityparking.pratteln.webservices.entity;

import java.util.ArrayList;
import java.util.List;

public class ParkingData {
    private List<Interval> intervals = new ArrayList<Interval>();

    public List<Interval> getIntervals() {
        return intervals;
    }

    /**
     * @param intervals The intervals
     */
    @SuppressWarnings("unused")
    public void setIntervals(List<Interval> intervals) {
        this.intervals = intervals;
    }
}
