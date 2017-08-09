package com.cityparking.pratteln.webservices.entity;

public class ResidentResponseBody {

    private boolean isResident = false;
    private ResidentTariff residentTariff;

    /**
     * @return The isResident
     */
    public boolean isResident() {
        return isResident;
    }

    /**
     * @param isResident The isResident
     */
    @SuppressWarnings("unused")
    public void setResident(boolean isResident) {
        this.isResident = isResident;
    }

    @Override
    public String toString() {
        return "ResidentResponseBody [isResident=" + isResident + "]";
    }

    /**
     * @return The residentTariff
     */
    public ResidentTariff getResidentTariff() {
        return residentTariff;
    }

    /**
     * @param residentTariff The residentTariff
     */
    @SuppressWarnings("unused")
    public void setResidentTariff(ResidentTariff residentTariff) {
        this.residentTariff = residentTariff;
    }

}
