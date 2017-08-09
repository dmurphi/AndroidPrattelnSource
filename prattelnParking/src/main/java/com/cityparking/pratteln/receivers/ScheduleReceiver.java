package com.cityparking.pratteln.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cityparking.pratteln.utils.PersistentUtil;

import java.util.Calendar;

/**
 * Created by Reea164 on 8/4/2016.
 */
public class ScheduleReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.HOUR_OF_DAY) < PersistentUtil.getStartInterval(context)) {
            PersistentUtil.clearPreviousParkingList(context);
        }
    }
}
