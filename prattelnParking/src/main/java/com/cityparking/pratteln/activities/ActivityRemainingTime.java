package com.cityparking.pratteln.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.text.Html;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cityparking.pratteln.R;
import com.cityparking.pratteln.constants.Constants;
import com.cityparking.pratteln.entities.Car;
import com.cityparking.pratteln.entities.Cars;
import com.cityparking.pratteln.entities.LastParking;
import com.cityparking.pratteln.entities.Taxes;
import com.cityparking.pratteln.listeners.GetBackFromWS;
import com.cityparking.pratteln.receivers.AlarmReceiver;
import com.cityparking.pratteln.titlebar.TitleBarHandler;
import com.cityparking.pratteln.utils.LogService;
import com.cityparking.pratteln.utils.PersistentUtil;
import com.cityparking.pratteln.utils.Tools;
import com.cityparking.pratteln.view.TimeView;
import com.cityparking.pratteln.webservices.WSCalls;
import com.cityparking.pratteln.webservices.entity.LocationGlobalDataResponseBody;

import net.simonvt.numberpicker.NumberPicker;
import net.simonvt.numberpicker.NumberPicker.OnValueChangeListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityRemainingTime extends Activity implements OnClickListener {

    private final static String TAG = ActivityRemainingTime.class.getSimpleName();

    private TimeView timeview;
    private CountDownTimer count;
    private AlarmManager alarmManager;
    private Timer t;
    private RelativeLayout timeV;
    private NumberPicker minutesNP;
    private EditText hoursInput, minutesInput;
    private TextView numberPickerTv;

    private PendingIntent pendingIntent;
    private int timeAdded = 0;
    private Intent mIntent;
    private Cars cars;
    private int hourOfDay;
    private ArrayList<Taxes> taxes = new ArrayList<Taxes>();
    private long millisToFinish = 0;
    private int reminder;// va fi ales
    private String mCost;
    private Integer hours = 1; // this 2 integers will be used to take time from the number pickers
    private Integer minutes = 0;
    private LocationGlobalDataResponseBody locationData;
    private int selectedLocationPosition = 0;
    private Car car;
    private int maxHours;
    private boolean mJustExtended = false;

    private RelativeLayout loader;

    // Define the listener for the slide in/out animations
    private Animation.AnimationListener animListener = new Animation.AnimationListener() {

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            // Get the text off the "new parking" text view
            String newParkingText = ((TextView) findViewById(R.id.time_new_parking_tv)).getText().toString();
            // Check to see whether the the animation was a slide in or slide out
            if (!newParkingText.contentEquals(getString(R.string.time_new_parking))) {
                // will start to extend time
                Calendar calendar = Calendar.getInstance();
                hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                int minOfDay = calendar.get(Calendar.MINUTE);
                LogService.log(TAG, "hourOfDay : " + hourOfDay + ": " + minOfDay);
                // checks to see if the parking lot is opened or not
                if (hourOfDay >= (PersistentUtil.getStartInterval(ActivityRemainingTime.this) / 60) && hourOfDay < (PersistentUtil.getStopInterval(ActivityRemainingTime.this) / 60)) {
                    LogService.log(TAG, "in the schedule!!!!!");
                    int minToFinish = (int) (millisToFinish / (1000 * 60));
                    int hours = minToFinish / 60;
                    // if the parking is going over the parking close time, the parking time will be extended with the until the parking will open
                    if ((hourOfDay + hours + (timeAdded / 60) > (PersistentUtil.getStopInterval(ActivityRemainingTime.this) / 60))) {
                        int toIntervalStart = 24 - ((PersistentUtil.getStopInterval(ActivityRemainingTime.this) / 60) - (PersistentUtil.getStartInterval(ActivityRemainingTime.this) / 60));
                        timeAdded = timeAdded + toIntervalStart * 60;
                        Toast.makeText(ActivityRemainingTime.this, getResources().getString(R.string.extended_until_parking) + (PersistentUtil.getStartInterval(ActivityRemainingTime.this) / 60), Toast.LENGTH_SHORT).show();
                    }
//                    LogService.log(TAG, "hour in extend:: " + (hourOfDay + hours));
//                    if (hourOfDay + hours > (PersistentUtil.getStartInterval(ActivityRemainingTime.this) / 60) && hourOfDay + hours < (PersistentUtil.getStopInterval(ActivityRemainingTime.this) / 60)) {
//                        int timeToStop = PersistentUtil.getStopInterval(ActivityRemainingTime.this) - (hourOfDay * 60 + minOfDay + minToFinish);
//                        int toIntervalStart = 24 - ((PersistentUtil.getStopInterval(ActivityRemainingTime.this) / 60) + (PersistentUtil.getStartInterval(ActivityRemainingTime.this) / 60));
//                        LogService.log(TAG, "to interval stop: " + toIntervalStart);
//                        if (timeToStop < timeAdded) {
//                            timeAdded = timeAdded + toIntervalStart * 60;
//                        }
//                    }
//                    LogService.log(TAG, "time to count after its modified: " + timeAdded);
                    // It was a slide IN
                    // Instantiate the intent and attach the required values to it
                    if (!mCost.equalsIgnoreCase("0")) {
                        startPaymentIntent();
                    }
                } else {
                    LogService.log(TAG, "out of the schedule!!!!!");
                    Toast.makeText(ActivityRemainingTime.this, getString(R.string.main_validation_parking_time_body_between), Toast.LENGTH_SHORT).show();
                }

            }
        }

        private void startPaymentIntent() { // will start the payment process, also will cancel the alarm manager
            Intent intent = new Intent(ActivityRemainingTime.this, ActivityPayment.class);
            intent.putExtra("extended", true);
            intent.putExtra("timeAdded", timeAdded + (millisToFinish / (1000 * 60)));
            intent.putExtra("newTime", timeAdded);
            intent.putExtra("cost", mCost);
            intent.putExtra("selectedLocationPosition", car.getParkedLocationId());
            intent.putExtra("maxParkingHours", maxParkingHour);
            startActivityForResult(intent, Constants.ACTIVITY_PAYMENT);
            LogService.log(TAG, "COST and timeadded is when entering: " + mCost + "timeadded: " + timeAdded);
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
//            finish();
        }
    };
    private Integer maxParkingHour = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_remaining_time);

        // initialize selected car
        cars = Tools.getCars(ActivityRemainingTime.this);
        for (Car carLoop : cars) {
            if (carLoop.getCnp().contentEquals(PersistentUtil.getSelectedCar(ActivityRemainingTime.this))) {
                car = carLoop;
            }
        }

        Constants.inPayment = false;
        LogService.log(TAG, "onCreate" + Constants.inPayment);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("intent_myaction_alarm");

        loader = (RelativeLayout) findViewById(R.id.splash_screen_loader_container);

        // Initialize global data of location
        locationData = PersistentUtil.getParkingLocation();
        selectedLocationPosition = car.getParkedLocationId();

        // gets data from the bundle
        getDataFromBundle();

        // Set the on click listeners for the buttons
        findViewById(R.id.time_new_parking).setOnClickListener(this);
        checkforExtendButtonAndSchedule();

        // Initialize the title bar
        TitleBarHandler.initTitleBar(this, getString(R.string.switzerland) + "  " + getString(R.string.munster), true, true);
        numberPickerTv = (TextView) findViewById(R.id.numberpicker_tv);
        TextView carplateTV = (TextView) findViewById(R.id.car_plate_textview);
        numberPickerTv.setOnClickListener(this);

        timeV = (RelativeLayout) findViewById(R.id.timeViewLayout);

        carplateTV.setText(PersistentUtil.getSelectedCar(ActivityRemainingTime.this));

        calculateCost();


        initTimeView();
    }

    private void getDataFromBundle() {
        mIntent = getIntent();
        if (mIntent != null) {
            if (mIntent.hasExtra("selectedcar")) {
                // will enter if a car is chosen from the cars list
                String selectedCar = mIntent.getStringExtra("selectedcar");
                PersistentUtil.setSelectedCar(ActivityRemainingTime.this, selectedCar);

            } else if (mIntent.hasExtra("timeToCount")) {
                // will enter when starting or extending a parking
                if (mIntent.getBooleanExtra("successMessage", false)) {
                    Toast.makeText(ActivityRemainingTime.this, getString(R.string.trans_suc), Toast.LENGTH_SHORT).show();
                }
                Long timeToCount = mIntent.getLongExtra("timeToCount", 0);
                reminder = mIntent.getIntExtra("reminder", 600);
                selectedLocationPosition = mIntent.getIntExtra("selectedLocationPosition", 0);

                if (timeToCount != 0) {
                    mJustExtended = true;
                    Constants.timeToCount = timeToCount;
                    car.setTimeToCount(timeToCount);
                    car.setParked(true);
                    if (selectedLocationPosition != 0) {
                        car.setParkedLocationId(selectedLocationPosition);
                    } else {
                        selectedLocationPosition = car.getParkedLocationId();
                    }
                    Tools.setCars(cars, ActivityRemainingTime.this);
                }

                LogService.log(TAG, "bundle NOT null: " + reminder + " || " + Constants.timeToCount);
                mIntent = null;
            } else {
                LogService.log(TAG, "-------------DOES NOT CONTAIN----------------------");
            }
        } else {
            LogService.log(TAG, "bundle null");
        }
        maxHours = getMaxParkingHour(selectedLocationPosition);
        if(locationData.getLocationParkingData().size() >= selectedLocationPosition) {
            if (maxHours < locationData.getLocationParkingData().get(selectedLocationPosition).getMaxParkingHours()) {
                findViewById(R.id.time_extend_time).setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    protected void onPause() {
        LogService.log(TAG, "onPause");
        if (count != null) {
            count.cancel();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogService.log(TAG, "onResume");
        if (mIntent == null) {
            // if no bundle, it will take the last selected car and initiate its time
            LogService.log(TAG, "resume bundle null");
            Constants.timeToCount = car.getTimeToCount();
            Constants.exactTime = car.getExactTime();

            timeview.setFullTime(Constants.timeToCount);
        }
        /*
         * Reset the footer to go back to default states
		 */
        // Set the text of the
        ((TextView) findViewById(R.id.time_new_parking_tv)).setText(getString(R.string.time_new_parking));
        // Show the time displayed above the footer
        // Reset the number pickers to zero
        // hoursNP.setValue(0);
        // minutesNP.setValue(0);

		/*
         * Reinitialize the reminder value
		 */
        // Get the index of the beed which was selected -1 or in interval [1, 5]
        int selectedIndex = PersistentUtil.getSelectedBeed(this);
        // Get the time of the beed
        reminder = Tools.getSecondsMin(0, Integer.parseInt(ActivityMain.getTimeFromBeedIndex(selectedIndex)));
        // Re-initialize the timer view
        LogService.log(TAG, "on resume reminder is: " + reminder);
        LogService.log(TAG, "on resume Time untill it will sound: " + ((System.currentTimeMillis() - Constants.exactTime) - (((Constants.timeToCount * 1000) - (reminder * 1000)))));
        if ((System.currentTimeMillis() - Constants.exactTime) < ((Constants.timeToCount * 1000) - (reminder * 1000))) {
            LogService.log(TAG, "on resume should be called: " + ((Constants.timeToCount * 1000) - (reminder * 1000)));
            if (PersistentUtil.getBoolean(this, PersistentUtil.REMINDER_ON)) {
                initAlarmReceiver((Constants.timeToCount * 1000) - (reminder * 1000));
            }
        }

        if (System.currentTimeMillis() - Constants.exactTime > 1000) {
            for (final Car car : cars) {
                LogService.log(TAG, "car from list cnp: " + car.getCnp() + " needs to be: " + PersistentUtil.getSelectedCar(ActivityRemainingTime.this));
                if (car.getCnp().contentEquals(PersistentUtil.getSelectedCar(ActivityRemainingTime.this))) {
                    final float begin = (System.currentTimeMillis() - car.getExactTime());
                    LogService.log(TAG, "Time EXACT starting time: " + car.getExactTime());
                    LogService.log(TAG, "Time: " + begin);
                    LogService.log(TAG, "TimeToCount: " + Constants.timeToCount * 1000);
                    if (begin < (car.getTimeToCount() * 1000)) {
                        // will init the timeview
                        LogService.log(TAG, "Countdown timer: " + ((Constants.timeToCount * 1000) - begin));
                        if (mJustExtended) {
                            timeToCount((int) ((Constants.timeToCount * 1000)));
                            mJustExtended = false;
                        } else {
                            timeToCount((int) ((Constants.timeToCount * 1000) - begin));
                        }
                    } else {
                        LogService.log(TAG, "Timer initiated in ELSE");
                        t = new Timer();
                        t.scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        if (timeview == null) {
                                            timeview = new TimeView(ActivityRemainingTime.this);
                                            tryPorterDuff();
                                            timeV.addView(timeview);
                                            timeview.setFullTime(Constants.timeToCount);
                                        }
                                        timeview.setTime(0);
                                        timeview.setTimeForText(-1, true);
                                        final float begin = (System.currentTimeMillis() - Constants.exactTime);
                                        int millisUntilFinished = (int) -((Constants.timeToCount * 1000) - begin);
                                        LogService.log(TAG, "intrat la millisUntilFinished" + millisUntilFinished);
                                        if (millisUntilFinished < 2100000000) {
                                            timeview.setTimeForText(millisUntilFinished, true);
                                        } else {
                                            LogService.log(TAG, "intrat in else la timertask");
                                            Constants.timeToCount = -1;
                                            Constants.exactTime = -1;
                                            car.setTimeToCount(Constants.timeToCount);
                                            car.setExactTime(Constants.timeToCount);
                                        }
                                    }
                                });
                            }
                        }, 0, 1000);
                    }
                    break;
                } else {
                    LogService.log(TAG, "not correct car!!");
                }

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.ACTIVITY_PAYMENT:
                if (resultCode == Constants.CLOSE_ACTIVITY)
                    finish();
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.time_new_parking:

                // If we've previously expanded the number picker drawer
                if (((TextView) findViewById(R.id.time_new_parking_tv)).getText().toString().contentEquals(getString(R.string.time_new_parking_save))) {
                /*
                 * Collapse the drawer only if the input data is valid
				 */
                    if (inputDataIsValid()) {
                        collapse(findViewById(R.id.footer_extend_slider));
                    }
                } else {
                /*
                 * Start a new parking
				 */
                    // Set the flag to indicate the timer has been canceled and new
                    // parking action is started
                    confirmNewParking();
                }

                break;
            case R.id.time_extend_time:

                // If we've previously expanded the number picker drawer
                if (((TextView) findViewById(R.id.time_new_parking_tv)).getText().toString().equalsIgnoreCase(getString(R.string.time_new_parking_save))) {
                    ((ImageView) findViewById(R.id.extend_arrow)).setImageResource(R.drawable.disclosure_arrow);
             /*
             * Collapse the drawer
             */
                    // Change the text of the "new parking" footer btn
                    ((TextView) findViewById(R.id.time_new_parking_tv)).setText(getString(R.string.time_new_parking));
                    // Colapse the drawer
                    collapse(findViewById(R.id.footer_extend_slider));
                } else {
                    if (ActivityRemainingTime.this.taxes != null && ActivityRemainingTime.this.taxes.size() > 0) {
                        ((ImageView) findViewById(R.id.extend_arrow)).setImageResource(R.drawable.disclosure_arrow_down);
                        // Hide the remaining time TV
                        // Change the text of the "new parking" footer btn
                        ((TextView) findViewById(R.id.time_new_parking_tv)).setText(getString(R.string.time_new_parking_save));
                        // Show the drawer
                        expand(findViewById(R.id.footer_extend_slider));
                    } else {
                        callParkingCharges();
//                        loader.setVisibility(View.INVISIBLE);
//                        ((ImageView) findViewById(R.id.extend_arrow)).setImageResource(R.drawable.disclosure_arrow_down);
//                        // Hide the remaining time TV
//                        // Change the text of the "new parking" footer btn
//                        ((TextView) findViewById(R.id.time_new_parking_tv)).setText(getString(R.string.time_new_parking_save));
//                        // Show the drawer
//                        expand(findViewById(R.id.footer_extend_slider));
                    }
                }

                break;
            case R.id.numberpicker_tv:
                callDialog();
                break;
        }

    }

    private void timeToCount(long time) {
        // will start the countdown timer
        count = new CountDownTimer(time, 1000) {

            public void onTick(long millisUntilFinished) {
                millisToFinish = millisUntilFinished;
                if (timeview == null) {
                    timeview = new TimeView(ActivityRemainingTime.this);
                    tryPorterDuff();
                    timeV.addView(timeview);
                    timeview.setFullTime(Constants.timeToCount);
                }
                timeview.setTime(millisUntilFinished / 1000);
                timeview.setTimeForText(millisUntilFinished, false);
            }

            public void onFinish() {
                timeview.setTimeForText(-1, true);
                timeview.setTime(0);
                // timeto.setText("Finished");
                // timeto.setTextColor(Color.RED);
                millisToFinish = 0;
                car.setTimeToCount(-1);
                car.setExactTime(-1);
                car.setParked(false);
                car.setParkedLocationId(-1);
                Tools.setCars(cars, ActivityRemainingTime.this);
            }
        }.start();
    }

    private void initTimeView() {
        timeview = new TimeView(this);
        tryPorterDuff();
        timeV.addView(timeview); // adds the timeview on the timeV relative layout
        timeview.setFullTime(Constants.timeToCount);
        if (reminder != 0) {
            timeToCount(Constants.timeToCount * 1000);
            LogService.log(TAG, "Reminder != 0" + reminder);
            Constants.exactTime = System.currentTimeMillis();

            car.setTimeToCount(Constants.timeToCount);
            car.setExactTime(Constants.exactTime);
            Tools.setCars(cars, ActivityRemainingTime.this);
            if (PersistentUtil.getBoolean(this, PersistentUtil.REMINDER_ON)) {
                initAlarmReceiver((Constants.timeToCount * 1000) - (reminder * 1000)); // initiates time for Alarm
            }
        } else {
            LogService.log(TAG, "Reminder == 0" + reminder);
        }
    }

    private void tryPorterDuff() {
        // if no hardware acceleration available, it will set the background to a specified color
        if (android.os.Build.VERSION.SDK_INT > 10) {

            try {
                Method setLayerTypeMethod = timeview.getClass().getMethod("setLayerType", new Class[]{int.class, Paint.class});
                setLayerTypeMethod.invoke(timeview, 1, null);
            } catch (NoSuchMethodException e) {
                // Older OS, no HW acceleration anyway
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            findViewById(R.id.remaining_time_main).setBackgroundColor(0xFFA3A187);
        }
    }

    private void initAlarmReceiver(long time) {
        Intent intent = new Intent(ActivityRemainingTime.this, AlarmReceiver.class);
        intent.setAction("intent_myaction_alarm");
        pendingIntent = PendingIntent.getBroadcast(ActivityRemainingTime.this, (int) Constants.exactTime, intent, 0);
        alarmManager = (AlarmManager) ActivityRemainingTime.this.getSystemService(ActivityRemainingTime.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, (Constants.exactTime + time), pendingIntent);
        LogService.log(TAG, "a fost initiat receiverul : " + (Constants.exactTime + time));
    }

    @Override
    protected void onDestroy() {
        LogService.log(TAG, "ondestroy activity remaining");
        if (t != null) {
            t.cancel();
        }
        super.onDestroy();
    }


    /**
     * Used to animate a view collapse. Reduce size from wrap content to 0.
     *
     * @param v
     */
    @SuppressWarnings("JavaDoc")
    public void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setAnimationListener(animListener);
        v.startAnimation(a);
    }

    /**
     * Used to animate a view expansion. Increase view from 0 to wrap content.
     *
     * @param v
     */
    public void expand(final View v) {
        v.measure(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        final int targtetHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 0;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1 ? RelativeLayout.LayoutParams.WRAP_CONTENT : (int) (targtetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (targtetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    private boolean inputDataIsValid() {

        // The parking time needs to be at least 1 minute
        if (hoursInput != null) {
            if (Integer.valueOf(hoursInput.getText().toString()) == 0) {
                Tools.showInfoDialog(this, getString(R.string.main_validation_parking_time_title), getString(R.string.main_validation_parking_time_body2));
                return false;
            }
        } else {
            Tools.showInfoDialog(this, getString(R.string.main_validation_parking_time_title), getString(R.string.main_validation_parking_time_body2));
            return false;
        }

        return true;
    }

    private void calculateCost() {
        // calculates the cost of the parking
        Calendar calendar = Calendar.getInstance();
        hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minOfDay = calendar.get(Calendar.MINUTE);
        LogService.log(TAG, "hourOfDay : " + hourOfDay + ": " + minOfDay);
        if (hoursInput != null && !hoursInput.getText().toString().equalsIgnoreCase("")) {
            hours = Integer.valueOf(hoursInput.getText().toString());
            minutes = Integer.valueOf(minutesInput.getText().toString());
        }
        // if maximum hours selected, minutes will be set on 0
        if (hours == PersistentUtil.getMaxHours(ActivityRemainingTime.this)) {
            minutes = 0;
            minutesNP.setValue(minutesNP.getMinValue());
        }
        timeAdded = Tools.getMinutes(hours, minutes);

        calculateFinalCost(timeAdded);
    }

    private void calculateFinalCost(final int min) {
        // if application first install, and there is no car number plate introduced hide cost value
        // we call computedCost method only to update parking period and some other stuff

        WSCalls.callComputedTariff(loader, this, car.getCnp(), selectedLocationPosition, min, new GetBackFromWS() {

            @Override
            public void success(ArrayList<Taxes> taxes) {

            }

            @Override
            public void success() {
                computedCost(Integer.valueOf(PersistentUtil.getString(ActivityRemainingTime.this, PersistentUtil.PARKING_DURATION_MINUTES)), PersistentUtil.getString(ActivityRemainingTime.this, PersistentUtil.FREE_PARKING_DURATION_MINUTES));
            }

            @Override
            public void fail(Integer Err) {

            }
        });

    }

    private void computedCost(int min, String freeParkingDuration) {

//        if (!freeParkingDuration.equalsIgnoreCase("0")) {
//            Toast.makeText(this, String.format(getString(R.string.free_parking_duration_in_minutes_toast), freeParkingDuration), Toast.LENGTH_SHORT).show();
//        }

        mCost = PersistentUtil.getString(this, PersistentUtil.COMPUTED_TARIFF);
        double costValue;

        if (mCost != null && !mCost.equalsIgnoreCase("")) {
            // handle back-end response for cost value with , and .
            costValue = Double.valueOf(mCost.contains(",") ? mCost.replace(",", ".") : mCost) / 100;
        } else {
            costValue = 0.0;
        }

//        if (cars != null) {
//            startPaymentIntent(cars, String.valueOf(costValue));
//        } else {
        PersistentUtil.setInt(this, Tools.getMinutes(min / 60, 0), "selected_parking_period");
        PersistentUtil.setBoolean(this, false, PersistentUtil.HAS_SPECIAL_TIME_LIMIT);
        showFinalCost(costValue);
//        }
    }

    private void showFinalCost(double chf) {
        String cost = "<b>";
        findViewById(R.id.main_cost_value).setVisibility(View.VISIBLE);
        DecimalFormat df = new DecimalFormat("0.00");

        if (locationData.getConvenienceFee().getConvenienceFeeAmount() != null) {
            chf += Double.valueOf(locationData.getConvenienceFee().getConvenienceFeeAmount().replace(",", "."));
        } else {
            Toast.makeText(ActivityRemainingTime.this, "Error parsing convenience fee!!", Toast.LENGTH_SHORT).show();
        }
        String currency = PersistentUtil.getCurrency(ActivityRemainingTime.this); // gets type of currency we use
        if (currency.equalsIgnoreCase("USD")) {
            cost += "$";
        } else if (currency.equalsIgnoreCase("EUR")) {
            cost += "€";
        } else {
            cost += "";
        }
        // if has flat rate but the time limit is over 24 hours
        cost += " " + df.format(chf);
        cost += "</b> ";
        LogService.log(TAG, "cost is: " + cost);
        ((TextView) findViewById(R.id.main_cost_value)).setText(Html.fromHtml(cost));
        numberPickerTv.setText(hours + " " + getString(R.string.main_parking_time_hour_unit) + " : " + minutes + " " + getString(R.string.main_parking_time_minute_unit) + " ");

    }


    private String allDayCost() {
        double chf = PersistentUtil.getAllDayCost(this);
        String currency = PersistentUtil.getCurrency(ActivityRemainingTime.this);
        timeAdded = Tools.getMinutes(24, 0);
        DecimalFormat df = new DecimalFormat("0.00");
        DecimalFormat df2 = new DecimalFormat("0");
        String cost = "<b>";
        if (currency.equalsIgnoreCase("USD")) {
            cost += "$";
        } else {
            cost += "";
        }
        if (currency.equalsIgnoreCase("USD")) {
            cost += "";
        } else {
            cost += currency;
        }
        cost += " " + df.format(chf);
        cost += "</b> ";
        ((TextView) findViewById(R.id.main_cost_value)).setText(Html.fromHtml(cost));
        numberPickerTv.setText(24 + " " + getString(R.string.main_parking_time_hour_unit) + " : " + 0 + " " + getString(R.string.main_parking_time_minute_unit) + " ");
        return df2.format(chf * 100);
    }

    private void callDialog() {

        // dialog for the numberpickers
        final Dialog dialog = new Dialog(ActivityRemainingTime.this);
        dialog.setContentView(R.layout.np_dialog);
        dialog.setTitle(getString(R.string.select_time));
        TextView allDay = (TextView) dialog.findViewById(R.id.select_all_day);
        allDay.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mCost = allDayCost();
                dialog.dismiss();

            }
        });
        NumberPicker hoursNP = (NumberPicker) dialog.findViewById(R.id.hour_number_picker);
        minutesNP = (NumberPicker) dialog.findViewById(R.id.min_number_picker);
        LinearLayout numberpicker = (LinearLayout) dialog.findViewById(R.id.np_layout);
        numberpicker.getLayoutParams().height = (int) (160 * getResources().getDisplayMetrics().density);

        Tools.initializeNumberPicker(hoursNP, 0, locationData.getLocationParkingData().get(selectedLocationPosition).getMaxParkingHours() - maxHours, 1, hours);
        Tools.initializeNumberPickerMinutes(minutesNP, 0, 60, PersistentUtil.getStepRate(ActivityRemainingTime.this));
        hoursNP.setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                calculateCost();
            }
        });
        minutesNP.setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                calculateCost();
            }
        });

        hoursInput = Tools.findInput(hoursNP);
        minutesInput = Tools.findInput(minutesNP);
        hoursInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        minutesInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        Button dialogButton = (Button) dialog.findViewById(R.id.okbutton);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateCost();
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private int getMaxParkingHour(int locationAreaId) {
        ArrayList<LastParking> previousParkingList = PersistentUtil.getPreviousParking(this);
        int previousParkingDurations = 0;
        if (previousParkingList != null && previousParkingList.size() > 0) {
            for (LastParking p : previousParkingList) {
                if (p.getCnp().equalsIgnoreCase(car.getCnp()) && p.getLocationAreaId() == locationAreaId) {
                    maxParkingHour = p.getMaxParkingHours();
                    previousParkingDurations = previousParkingDurations + (Integer.valueOf(p.getLastParkingDuration()) / (60 * 60));
                }
            }

        }
        return (maxParkingHour - previousParkingDurations);
    }

    public void confirmNewParking() {
        // dialog to ask if you want to start new parking
        final AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRemainingTime.this);
        builder.setTitle(getString(R.string.confirm_new_parking_title));
        builder.setMessage(getString(R.string.confirm_new_parking_description));

        builder.setPositiveButton(getString(R.string.confirm_new_parking_positive), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (alarmManager != null) {
                    alarmManager.cancel(pendingIntent);
                }
                car.setTimeToCount(-1);
                car.setExactTime(-1);
                car.setParked(false);
                car.setParkedLocationId(-1);
                Tools.setCars(cars, ActivityRemainingTime.this);

                startActivity(new Intent(ActivityRemainingTime.this, ActivityMain.class));
                finish();
            }
        });

        builder.setNegativeButton(getString(R.string.confirm_new_parking_negavive), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        if (Looper.myLooper() == Looper.getMainLooper()) {
            builder.show();

        }
    }

    private void callParkingCharges() { // ws call to get parking charges
        loader.setVisibility(View.VISIBLE);
        WSCalls.webserviceCallParkingCharges(loader, ActivityRemainingTime.this, new GetBackFromWS() {

            @Override
            public void success() {
            }

            @Override
            public void success(ArrayList<Taxes> taxes) {
                if (ActivityRemainingTime.this.taxes != null) {
                    ActivityRemainingTime.this.taxes.clear();
                }
                for (int i = 0; i < taxes.size(); i++) {
                    Taxes tax = new Taxes(taxes.get(i).getPrice(), taxes.get(i).getMinutes() - (Constants.timeToCount / 60));
                    ActivityRemainingTime.this.taxes.add(tax);
                }
                loader.setVisibility(View.INVISIBLE);
                ((ImageView) findViewById(R.id.extend_arrow)).setImageResource(R.drawable.disclosure_arrow_down);
                // Hide the remaining time TV
                // Change the text of the "new parking" footer btn
                ((TextView) findViewById(R.id.time_new_parking_tv)).setText(getString(R.string.time_new_parking_save));
                // Show the drawer
                expand(findViewById(R.id.footer_extend_slider));

            }

            @Override
            public void fail(Integer Err) {
                loader.setVisibility(View.INVISIBLE);
                switch (Err) {
                    case 0:
                        callParkingCharges();
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

    private void checkforExtendButtonAndSchedule() {
        Calendar calendar = Calendar.getInstance();
        hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minOfDay = calendar.get(Calendar.MINUTE);
        int secOfDay = calendar.get(Calendar.SECOND);
        LogService.log(TAG, "hourOfDay : " + hourOfDay + ": " + minOfDay);
        // if parking lot is closed, it will not allow you to extend the time
//        if (hourOfDay >= PersistentUtil.getStartInterval(ActivityRemainingTime.this) && hourOfDay < PersistentUtil.getStopInterval(ActivityRemainingTime.this)) {
        findViewById(R.id.time_extend_time).setOnClickListener(this);
        findViewById(R.id.free_time).setVisibility(View.INVISIBLE);
//        } else {
//            long timeto0 = 0;
//            if (hourOfDay < PersistentUtil.getStartInterval(ActivityRemainingTime.this)) {
//                timeto0 = PersistentUtil.getStartInterval(ActivityRemainingTime.this) * 60 - (hourOfDay * 60 + minOfDay); // will get the time that needs to pass untill the parking opens
//            } else {
//                timeto0 = 24 * 60 - (hourOfDay * 60 + minOfDay) + PersistentUtil.getStartInterval(ActivityRemainingTime.this) * 60;
//            }
//            // TODO BOTI : this is the textView that will let the user know that the clock is out of the schedule, here it's visible (out of schedule)
//            String timeS = getTimeForFreeParking(timeto0 * 60 * 1000); // needs to be millis
//            findViewById(R.id.free_time).setVisibility(View.VISIBLE);
//            ((TextView) findViewById(R.id.free_time)).setText(getString(R.string.free_time) + timeS);
//            findViewById(R.id.time_extend_time).setBackgroundColor(Color.parseColor("#505050"));*/
//        }
    }

    public String getTimeForFreeParking(long time) {
        String timeS = "";
        int hours = (int) (time / (60 * 60 * 1000));
        int hoursMod = (int) (time % (60 * 60 * 1000));
        int min = (int) (hoursMod / (1000 * 60));
        LogService.log(TAG, "hourss: " + hours + " min " + min);
        LogService.log(TAG, "hourss mod: " + hoursMod);
        if (hours != 0) {
            timeS = hours + "h " + min + "m ";
        } else if (min != 0) {
            timeS = min + "m ";
        }

        return timeS;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) { // acts as the on back pressed handler
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            LogService.log(TAG, "onbackpressed!!! ACTIVITY_MAIN");
            switch (PersistentUtil.getMainPageForBack(ActivityRemainingTime.this)) {
                case PersistentUtil.ACTIVITY_MAIN:
                    LogService.log(TAG, "onbackpressed!!!");
                    startActivity(new Intent(ActivityRemainingTime.this, ActivityMain.class));
                    finish();
                    break;
                case PersistentUtil.ACTIVITY_MY_CARS_LIST:
                    LogService.log(TAG, "onbackpressed!!! ACTIVITY_MY_CARS_LIST");
                    Intent intent = new Intent(ActivityRemainingTime.this, ActivityMyCarsList.class);
                    intent.putExtra("gototimer", true);
                    startActivity(intent);
                    finish();
                    break;
                case PersistentUtil.ACTIVITY_REMAINING_TIME:
                    LogService.log(TAG, "onbackpressed!!! ACTIVITY_REMAINING_TIME");
                    // Tools.showCloseAppConfirmation(ActivityRemainingTime.this);
                    ActivityRemainingTime.this.startActivity(new Intent(ActivityRemainingTime.this, ActivityMyCarsList.class));
                    ActivityRemainingTime.this.finish();
                    break;
                default:
                    break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
