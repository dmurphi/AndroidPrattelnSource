package com.cityparking.pratteln.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;

import com.cityparking.pratteln.activities.ActivityMain;
import com.cityparking.pratteln.activities.ActivityRemainingTime;
import com.cityparking.pratteln.constants.Constants;
import com.cityparking.pratteln.entities.Cars;
import com.cityparking.pratteln.utils.ForegroundCheckTask;
import com.cityparking.pratteln.utils.LogService;
import com.cityparking.pratteln.utils.PersistentUtil;
import com.cityparking.pratteln.utils.Tools;

import java.util.concurrent.ExecutionException;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent k2) {
		LogService.log("RECEIVER", "ALARM RECEIVED!!!");
		try {
			boolean foreground = new ForegroundCheckTask().execute(context).get();
			Cars cars = Tools.getCars(context);
			String selectedCar = PersistentUtil.getSelectedCar(context);
			int selectedIndex = PersistentUtil.getSelectedBeed(context);
			// Get the time of the beed
			int reminder = Tools.getSecondsMin(0, Integer.parseInt(ActivityMain.getTimeFromBeedIndex(selectedIndex)));
			for (int i = 0; i < cars.size(); i++) {
				final float begin = (System.currentTimeMillis() - cars.get(i).getExactTime());
				float time = ((cars.get(i).getTimeToCount() * 1000) - begin);
				if (Math.abs((reminder * 1000) - time) < 1000) {
					selectedCar = cars.get(i).getCnp();
					Constants.timeToCount = cars.get(i).getTimeToCount();
					break;
				}
			}
			if (!foreground) { // if app is not in foreground it will bring it to the foreground
				Intent i = new Intent(context, ActivityRemainingTime.class);
				i.putExtra("selectedcar", selectedCar);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				context.startActivity(i);

			}

			Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM); // tries to get alarm sound
			if (notification == null) {
				notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				if (notification == null) {
					notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
				}
			}
			final Ringtone r = RingtoneManager.getRingtone(context, notification);
			r.play();
			Handler han = new Handler();
			han.postDelayed(new Runnable() {

				@Override
				public void run() {
					r.stop();
				}
			}, 5000); // will start alarm
		} catch (InterruptedException e) {
			LogService.log("", "error:" + e);
		} catch (ExecutionException e) {
			LogService.log("", "error:" + e);
		} catch (Exception e) {
			LogService.log("", "error:" + e);
		}

	}
}
