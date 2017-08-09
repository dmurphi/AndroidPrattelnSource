package com.cityparking.pratteln.webservices.entity;

public class Interval {
    private Integer start;
    private Integer end;
    private Integer price;
    private Integer dayOfWeek;

    public Integer getStart() {
        return start;
    }

    @SuppressWarnings("unused")
    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    @SuppressWarnings("unused")
    public void setEnd(Integer end) {
        this.end = end;
    }

    @SuppressWarnings("unused")
    public Integer getPrice() {
        return price;
    }

    @SuppressWarnings("unused")
    public void setPrice(Integer price) {
        this.price = price;
    }

    @SuppressWarnings("unused")
    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    @SuppressWarnings("unused")
    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

}
