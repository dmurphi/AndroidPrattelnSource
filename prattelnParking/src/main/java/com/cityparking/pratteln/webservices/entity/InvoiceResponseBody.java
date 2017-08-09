package com.cityparking.pratteln.webservices.entity;

import com.cityparking.pratteln.entities.UserPayments;

import java.util.ArrayList;

public class InvoiceResponseBody {
    private Boolean hasMore;
    private ArrayList<UserPayments> userPayments = new ArrayList<UserPayments>();
    private Integer noOfItems;

    public Boolean getHasMore() {
        return hasMore;
    }

    @SuppressWarnings("unused")
    public void setHasMore(Boolean hasMore) {
        this.hasMore = hasMore;
    }

    public ArrayList<UserPayments> getUserPayments() {
        return userPayments;
    }

    @SuppressWarnings("unused")
    public void setUserPayments(ArrayList<UserPayments> userPayments) {
        this.userPayments = userPayments;
    }

    @SuppressWarnings("unused")
    public Integer getNoOfItems() {
        return noOfItems;
    }

    @SuppressWarnings("unused")
    public void setNoOfItems(Integer noOfItems) {
        this.noOfItems = noOfItems;
    }
}
