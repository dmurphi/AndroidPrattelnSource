package com.cityparking.pratteln.activities;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.cityparking.pratteln.R;
import com.cityparking.pratteln.constants.Constants;
import com.cityparking.pratteln.entities.Car;
import com.cityparking.pratteln.entities.Cars;
import com.cityparking.pratteln.entities.Taxes;
import com.cityparking.pratteln.listeners.GetBackFromWS;
import com.cityparking.pratteln.receivers.ScheduleReceiver;
import com.cityparking.pratteln.utils.LogService;
import com.cityparking.pratteln.utils.PersistentUtil;
import com.cityparking.pratteln.utils.Tools;
import com.cityparking.pratteln.webservices.WSCalls;

public class ActivitySplashScreen extends Activity {

    private static final String TAG = ActivitySplashScreen.class.getSimpleName();

    private static final int GO_TO_MAIN = 1;

    private static final int GO_TO_TIMER = 2;

    private int actionAfterDelay;

    private RelativeLayout loader;

    // Keeps track of the number of responses got from webservice calls. USed in deciding when all the data was retrieved successfully.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_splash_screen);

        loader = (RelativeLayout) findViewById(R.id.splash_screen_loader_container);
        loader.setVisibility(View.VISIBLE);

        // Get the values of timetocount and exact time from the shared preferences
        Cars cars = Tools.getCars(ActivitySplashScreen.this);
        for (Car car : cars) {
            if (car.getCnp().contentEquals(PersistentUtil.getSelectedCar(ActivitySplashScreen.this))) {
                Constants.timeToCount = car.getTimeToCount();
                Constants.exactTime = car.getExactTime();
            }

        }
        if (PersistentUtil.getSelectedBeed(this) == -1) {
            PersistentUtil.setSelectedBeed(this, 2);
        }
        LogService.log(TAG, "hei SPLASH timeto countL " + Constants.timeToCount);
        LogService.log(TAG, "hei SPLASH exact time: " + Constants.exactTime);

        if (PersistentUtil.getFirstRun(ActivitySplashScreen.this)) {
            // if first run it will create a user and guid
            PersistentUtil.setFreeParking(this, 0);
            PersistentUtil.setBoolean(this, false, PersistentUtil.WHITE_LIST_CNP);
            LogService.log(TAG, "is first run");
            WSCalls.createInitialUser(loader, ActivitySplashScreen.this, new GetBackFromWS() {

                @Override
                public void success(ArrayList<Taxes> taxes) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void success() {
                    PersistentUtil.setFirstRun(ActivitySplashScreen.this, false);
                    goToWSCalls();
                }

                @Override
                public void fail(Integer Err) {
                    // TODO Auto-generated method stub

                }
            });

            Intent intent = new Intent(this, ScheduleReceiver.class);
            intent.setAction("clear_previous_parkings");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                    0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 24);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            alarm.cancel(pendingIntent);
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        } else {
            LogService.log(TAG, "is NOT first run");
            goToWSCalls();
        }
    }

    private void goToWSCalls() {
        // If the timer has already been started, then jump to timer activity
        if (PersistentUtil.getTimerStarted(this)) {
            actionAfterDelay = GO_TO_TIMER;
            // makeWebserviceCall();
            callForLocations();
            // createDelayBeforeContinuing();
        } else
        // Otherwise make a webservice call to retrieve required information
        {
            actionAfterDelay = GO_TO_MAIN;
            callForLocations();
            // makeWebserviceCall();
        }
    }

    private void callForLocations() {
        WSCalls.callLocationsGlobalData(loader, ActivitySplashScreen.this, new GetBackFromWS() {

            @Override
            public void success(ArrayList<Taxes> taxes) {
                // TODO Auto-generated method stub
            }

            @Override
            public void success() {
                createDelayBeforeContinuing();
            }

            @Override
            public void fail(Integer Err) {
                switch (Err) {
                    case 0:
                        callForLocations();
                        break;
                    case 1:
                        finish();
                        break;

                    default:
                        break;
                }
            }
        });
    }

    /**
     * It simulates a N (Note: see Constants) seconds delay before continuing to the next activity. This is done so the splash screen won't appear and disappear very quickly when connection to webservice is done very quickly.
     * <p/>
     * This should be called ONLY after the required information has already been retrieved from the webservice.
     */
    private void createDelayBeforeContinuing() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (actionAfterDelay == GO_TO_MAIN) {
                    startActivity(new Intent(ActivitySplashScreen.this, ActivityMain.class));
                    finish();
                } else if (actionAfterDelay == GO_TO_TIMER) {
                    Intent intent = new Intent(ActivitySplashScreen.this, ActivityMyCarsList.class);
                    intent.putExtra("gototimer", true);
                    startActivity(intent);
                    finish();
                }

            }
        }, Constants.SIMULATED_DELAY);
    }

}
