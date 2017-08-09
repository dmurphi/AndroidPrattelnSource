package com.cityparking.pratteln.listeners;

import com.cityparking.pratteln.entities.UserPayments;

import java.util.ArrayList;

public interface GetBackFromInvoices {

	public void success();

	public void success(ArrayList<UserPayments> invoices);

}
