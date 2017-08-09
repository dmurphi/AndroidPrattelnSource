package com.cityparking.pratteln.webservices.entity;

public class FreeParkingResponseBody {

    private Integer freeParkingDuration;
    private Integer freeParkingDirectiveId;

    /**
     * @return The freeParkingDuration
     */
    public Integer getFreeParkingDuration() {
        return freeParkingDuration;
    }

    /**
     * @param freeParkingDuration The freeParkingDuration
     */
    @SuppressWarnings("unused")
    public void setFreeParkingDuration(Integer freeParkingDuration) {
        this.freeParkingDuration = freeParkingDuration;
    }

    /**
     * @return The freeParkingDirectiveId
     */
    public Integer getFreeParkingDirectiveId() {
        return freeParkingDirectiveId;
    }

    /**
     * @param freeParkingDirectiveId The freeParkingDirectiveId
     */
    @SuppressWarnings("unused")
    public void setFreeParkingDirectiveId(Integer freeParkingDirectiveId) {
        this.freeParkingDirectiveId = freeParkingDirectiveId;
    }

}
