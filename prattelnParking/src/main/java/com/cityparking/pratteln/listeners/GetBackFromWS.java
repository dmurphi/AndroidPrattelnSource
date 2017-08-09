package com.cityparking.pratteln.listeners;

import java.util.ArrayList;

import com.cityparking.pratteln.entities.Taxes;

public interface GetBackFromWS {

	public void success();

	public void success(ArrayList<Taxes> taxes);

	public void fail(Integer Err);

}
