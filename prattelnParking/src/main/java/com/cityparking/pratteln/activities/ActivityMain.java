package com.cityparking.pratteln.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
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
import com.cityparking.pratteln.picker.ArrayWheelAdapter;
import com.cityparking.pratteln.picker.OnWheelChangedListener;
import com.cityparking.pratteln.picker.OnWheelScrollListener;
import com.cityparking.pratteln.picker.WheelView;
import com.cityparking.pratteln.receivers.AlarmReceiver;
import com.cityparking.pratteln.titlebar.TitleBarHandler;
import com.cityparking.pratteln.utils.LogService;
import com.cityparking.pratteln.utils.PersistentUtil;
import com.cityparking.pratteln.utils.Tools;
import com.cityparking.pratteln.webservices.WSCalls;
import com.cityparking.pratteln.webservices.entity.LocationGlobalDataResponseBody;
import com.cityparking.pratteln.webservices.entity.LocationParkingDatum;
import com.cityparking.pratteln.webservices.entity.ResidentTariff;
import com.octo.android.robodemo.LabeledPoint;
import com.octo.android.robodemo.RoboDemo;

import net.simonvt.numberpicker.NumberPicker;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ActivityMain extends Activity implements OnClickListener, OnFocusChangeListener {

    public static String TAG = ActivityMain.class.getSimpleName();

    private RelativeLayout loader;
    private NumberPicker minutesNP; // the number pickers used in the application
    private TextView location, numberPickerTv, carNumberPlate;
    private RelativeLayout pickerContainer;
    private EditText hoursInput, minutesInput;

    private String[] values;
    private ArrayList<LocationParkingDatum> locationParking;
    private LocationParkingDatum selectedLocationData;
    private LocationGlobalDataResponseBody locationData;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private int periodInMinutes; // period of time chosen by user, in minutes
    private int hourOfDay;
    private Integer hours = 1; // this 2 integers will be used to take time from the number pickers
    private Integer minutes = 0;
    private int reminder = 15;
    private int dayOfWeek = 0;
    private int selectedLocationAreaId = 0;
    private boolean wheelScrolled = false;

    private Cars cars;
    private Car selectedCar = null;
    private String car = "";
    private final static String KEY_HELP_OVERLAY = "help_overlay";

    private int maxParkingHours = 0;
    private boolean dialogShouldCall;
    private int min;

    private interface getBackFromConfirmation {
        void newParking();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        cars = Tools.getCars(ActivityMain.this);

        Calendar calendar = Calendar.getInstance();
        dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (dayOfWeek == 0) {
            dayOfWeek = 7;
        }

        locationData = PersistentUtil.getParkingLocation();

        // Initialize the title bar
        TitleBarHandler.initTitleBar(this, getString(R.string.switzerland) + "  " + getString(R.string.munster), false, true);

        // Initializing views
        loader = (RelativeLayout) findViewById(R.id.splash_screen_loader_container);
        carNumberPlate = (TextView) findViewById(R.id.car_plate_number);
        TextView startParking = (TextView) findViewById(R.id.footer_text_start_parking);
        TextView startFreeParking = (TextView) findViewById(R.id.footer_text_start_free_parking);

        ((TextView) findViewById(R.id.free_parking_time_notif)).setText(getString(R.string.free_parking) + " " + (PersistentUtil.getStopInterval(ActivityMain.this) / 60) + ":00" + " " + getString(R.string.to) + " " + (PersistentUtil.getStartInterval(ActivityMain.this) / 60) + ":00" + ".");
        numberPickerTv = (TextView) findViewById(R.id.numberpicker_tv);
        location = (TextView) findViewById(R.id.location_name);

        pickerContainer = (RelativeLayout) findViewById(R.id.picker_container);
        TextView pickerDone = (TextView) findViewById(R.id.picker_done_tv);

        // Set up listeners
        carNumberPlate.setOnClickListener(this);
        startParking.setOnClickListener(this);
        startFreeParking.setOnClickListener(this);
        numberPickerTv.setOnClickListener(this);
        pickerContainer.setOnClickListener(this);
        pickerDone.setOnClickListener(this);
        location.setOnFocusChangeListener(this);
        location.setOnTouchListener(new OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                location.setInputType(InputType.TYPE_NULL);
                initBayPicker(0);
                return false;
            }
        });

        car = PersistentUtil.getSelectedCar(ActivityMain.this);
        // Check if the selected car's parking time is expired
        for (Car c : cars) {
            if (c.getCnp().contentEquals(car)) {
                selectedCar = c;
                if (selectedCar.getExactTime() < 0) {
                    selectedCar.setParked(false);
                } else {
                    selectedCar.setParked(true);
                }
            }
        }

        locationParking = new ArrayList<LocationParkingDatum>(locationData.getLocationParkingData());
        selectedLocationData = locationParking.get(0);

        if (!car.contentEquals("-1")) { // -1 = default value, else CNP
            carNumberPlate.setText(car);
            getSpecialResident(0);
        } else {
            initLocations(1, 0);
        }

        showForced();
    }

    /**
     * Initialize Locations (if index == 0 is special resident else if index == 1 is not special)
     *
     * @param index
     * @param newValue
     */
    @SuppressWarnings("JavaDoc")
    private void initLocations(int index, int newValue) {
        if (locationData != null) {
            values = new String[locationData.getLocationParkingData().size()];
            int i = 0;
            for (LocationParkingDatum l : locationParking) {
                values[i] = l.getLocationName();
                ++i;
            }
            try {
                Constants.VAT = locationData.getVat().getVAT();
            } catch (NullPointerException e) {
                Log.d(TAG, "Problem occurred: " + e.getMessage(), e);
            }
        }

        if (values.length > 0) {
            location.setText(values[newValue]);
        } else {
            location.setText("Empty");
        }
        initGlobalParameters(newValue, index);

    }

    /**
     * used to verify if the selected car number plate is a special one or not.
     *
     * @param newValue
     */
    @SuppressWarnings("JavaDoc")
    private void getSpecialResident(final int newValue) {
        WSCalls.callSpecialResident(loader, this, carNumberPlate.getText().toString(), locationParking.get(newValue).getLocationId(), new GetBackFromWS() {

            @Override
            public void success(ArrayList<Taxes> taxes) {

            }

            @Override
            public void success() {
                if (PersistentUtil.getInt(ActivityMain.this, "selected_location_pos") != -1) {
                    initLocations(PersistentUtil.getResident(ActivityMain.this) ? 0 : 1, PersistentUtil.getInt(ActivityMain.this, "selected_location_pos"));
                } else {
                    initLocations(PersistentUtil.getResident(ActivityMain.this) ? 0 : 1, newValue);
                }
            }

            @Override
            public void fail(Integer Err) {

            }
        });

    }

    /**
     * initialize main screen attributes and calculate taxes index is used to show if is special resident with special taxes (0 - special / 1 - normal)
     *
     * @param position
     * @param index
     */
    @SuppressWarnings("JavaDoc")
    private void initGlobalParameters(int position, int index) {
        PersistentUtil.setInt(this, position, "selected_location_pos");
        if (locationParking.size() > 0 && locationParking.size() >= position) {
            selectedLocationAreaId = locationParking.get(position).getLocationId();
            PersistentUtil.setCurrency(ActivityMain.this, locationData.getCurrency());
            // uses position of current day in the interval array
            switch (index) {
                case 0:
                    ResidentTariff specialResidentTariff = PersistentUtil.getResidentLocation();
                    PersistentUtil.setStartInterval(ActivityMain.this, specialResidentTariff.getParkingData().getIntervals().get(0).getStart());
                    PersistentUtil.setStopInterval(ActivityMain.this, specialResidentTariff.getParkingData().getIntervals().get(dayOfWeek).getEnd());
                    maxParkingHours = specialResidentTariff.getMaxParkingHours();
                    PersistentUtil.setStepRate(ActivityMain.this, specialResidentTariff.getStep());
                    // uses position of current day in the interval array
                    break;
                case 1:
                    PersistentUtil.setStartInterval(ActivityMain.this, locationParking.get(position).getParkingData().getIntervals().get(dayOfWeek).getStart());
                    PersistentUtil.setStopInterval(ActivityMain.this, locationParking.get(position).getParkingData().getIntervals().get(dayOfWeek).getEnd());
                    maxParkingHours = locationParking.get(position).getMaxParkingHours();
                    PersistentUtil.setStepRate(ActivityMain.this, locationParking.get(position).getStep());
                    break;
                default:
            }

            dialogShouldCall = true;
            calculateCost(null);
            getMaxParkingHour();
        }
    }

    private void getMaxParkingHour() {
        ArrayList<LastParking> previousParkingList = PersistentUtil.getPreviousParking(this);
        int previousParkingDurations = 0;
        if (previousParkingList != null && previousParkingList.size() > 0) {
            for (LastParking p : previousParkingList) {
                if (p.getCnp().equalsIgnoreCase(numberPickerTv.getText().toString()) && p.getLocationAreaId() == selectedLocationAreaId) {
                    maxParkingHours = p.getMaxParkingHours();
                    previousParkingDurations = previousParkingDurations + (Integer.valueOf(p.getLastParkingDuration()) / (60 * 60));
                }
            }

        }
        maxParkingHours = maxParkingHours - (previousParkingDurations);
    }

    private void showForced() {
        if (PersistentUtil.getShowHelp(ActivityMain.this)) {
            displayHelpOverlay(true);
            PersistentUtil.setShowHelp(ActivityMain.this, false);
        } else {
            displayHelpOverlay(false);
        }
    }

    /**
     * Initializes wheel picker for location
     *
     * @param currentPos
     */
    @SuppressWarnings("JavaDoc")
    private void initBayPicker(int currentPos) {
        pickerContainer.setVisibility(View.VISIBLE);
        WheelView locationPicker = (WheelView) findViewById(R.id.bay_wheel_picker);
        locationPicker.setViewAdapter(new ArrayWheelAdapter<Object>(this, values));
        locationPicker.setVisibleItems(2);
        locationPicker.setCurrentItem(currentPos);
        locationPicker.addChangingListener(changedListener);
        locationPicker.addScrollingListener(scrolledListener);
    }

    // Wheel changed listener
    private final OnWheelChangedListener changedListener = new OnWheelChangedListener() {
        public void onChanged(WheelView wheel, int oldValue, int newValue) {
            if (!wheelScrolled) {
                updateStatus(newValue);
            }
        }
    };

    // Update edit text string
    private void updateStatus(int newValue) {
        if (values.length > 0) {
            location.setText(values[getWheel(R.id.bay_wheel_picker).getCurrentItem()]);
            PersistentUtil.setInt(this, Tools.getMinutes(1, 0), "selected_parking_period");//reset selected period to 1h
            if (!car.contentEquals("-1")) { // -1 = default value, else CNP
                getSpecialResident(newValue);
            }
            initGlobalParameters(newValue, PersistentUtil.getResident(ActivityMain.this) ? 0 : 1);
            selectedLocationData = locationParking.get(newValue);
        }

    }

    // Wheel scrolled listener
    OnWheelScrollListener scrolledListener = new OnWheelScrollListener() {

        @Override
        public void onScrollingStarted(WheelView wheel) {
            wheelScrolled = true;

        }

        @Override
        public void onScrollingFinished(WheelView wheel) {
            wheelScrolled = false;
            updateStatus(wheel.getCurrentItem());

        }
    };

    /**
     * Returns wheel by Id
     *
     * @param id the wheel Id
     * @return the wheel with passed Id
     */
    private WheelView getWheel(int id) {
        return (WheelView) findViewById(id);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // handle expired parking on onResume
        if (selectedCar != null) {
            long startTime = selectedCar.getExactTime();
            long parkingInterval = selectedCar.getTimeToCount();
            long finishTime = startTime + (parkingInterval * 1000);
            long currentTime = System.currentTimeMillis();
            if (finishTime < currentTime) {
                selectedCar.setExactTime(-1);
                selectedCar.setTimeToCount(-1);
                selectedCar.setParkedLocationId(-1);
                selectedCar.setParked(false);
            }
        }
        // Set the reminder time
        setReminderTime();
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

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // if back pressed will show close app popup
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Tools.showCloseAppConfirmation(this);
            PersistentUtil.setInt(ActivityMain.this, -1, "selected_location_pos");
            PersistentUtil.setInt(this, -1, "selected_parking_period");
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.footer_text_start_parking: // if the input data is valid, it will start the payment process
                if (inputDataIsValid()) {
                    boolean isUsed = selectedCar.isParked();
                    if (isUsed) {
                        confirmNewParking(1);
                    } else {
                        startPaymentIntent(cars, PersistentUtil.getString(ActivityMain.this, PersistentUtil.COMPUTED_TARIFF));
                    }
                }
                break;
            case R.id.footer_text_start_free_parking: // if the input data is valid, it will start free parking

                boolean isUsedFree = false;
                String carCnpFree = carNumberPlate.getText().toString().trim();
                for (Car car : cars) {
                    if (car.getCnp().equalsIgnoreCase(carCnpFree)) {
                        isUsedFree = car.isParked();
                    }
                }
                if (inputDataIsValid()) {
                    if (isUsedFree) {
                        confirmNewParking(0);
                    } else {
                        getFreeParkingAvailable(0, selectedCar.getCnp(), new getBackFromConfirmation() {

                            @Override
                            public void newParking() {

                            }
                        });
                    }
                }
                break;
            case R.id.numberpicker_tv: // will call the number picker dialog, so you can choose the time to park
                callDialog();
                break;
            case R.id.car_plate_number:
                // goes to the list of cars
                PersistentUtil.setInt(this, Tools.getMinutes(hours, minutes), "selected_parking_period");
                Intent intent = new Intent(ActivityMain.this, ActivityMyCarsList.class);
                intent.putExtra("gototimer", false);
                startActivity(intent);
                finish();
                break;
            case R.id.picker_done_tv:
                location.clearFocus();
                pickerContainer.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    // confirm new parking for a car which is already parking
    public void confirmNewParking(final int type) {
        // dialog to ask if you want to start new parking
        final AlertDialog.Builder builder = new AlertDialog.Builder(ActivityMain.this);
        builder.setTitle(getString(R.string.confirm_new_parking_title));
        builder.setMessage(getString(R.string.confirm_new_parking_description));
        builder.setPositiveButton(getString(R.string.confirm_new_parking_positive), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                if (type == 1) {
                    stopParking();
                    startPaymentIntent(cars, PersistentUtil.getString(ActivityMain.this, PersistentUtil.COMPUTED_TARIFF));
                } else {
                    getFreeParkingAvailable(type, carNumberPlate.getText().toString().trim(), new getBackFromConfirmation() {

                        @Override
                        public void newParking() {
                            if (PersistentUtil.getFreeParking(ActivityMain.this) > 0) {
                                stopParking();
                                startFreeParking();
                            } else {
                                Toast.makeText(ActivityMain.this, getResources().getString(R.string.freetime_used), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                dialog.dismiss();
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

    private void stopParking() {
        Cars cars = Tools.getCars(ActivityMain.this);
        for (Car car : cars) {
            if (car.getCnp().contentEquals(PersistentUtil.getSelectedCar(ActivityMain.this))) {
                if (alarmManager != null) {
                    alarmManager.cancel(pendingIntent);
                }
                car.setTimeToCount(-1);
                car.setParkedLocationId(-1);
                car.setExactTime(-1);
                car.setParked(false);
            }
        }
        Tools.setCars(cars, ActivityMain.this);
    }

    /**
     * Sets the alarm reminder text
     */
    private void setReminderTime() {

        // Get the index of the beed which was selected -1 or in interval [1, 5]
        int selectedIndex = PersistentUtil.getSelectedBeed(this);

        // Get the time of the beed
        String time = ActivityMain.getTimeFromBeedIndex(selectedIndex);

        String str = "";
        if (time.contentEquals("0")) {
            str += getString(R.string.reminder2);
            str += "<b><font color='black'> ";
            str += getString(R.string.not2);
            str += " </font></b>";
            str += getString(R.string.set);
        } else {
            str += getString(R.string.reminder_set_to);
            str += "<b><font color='black'> ";
            str += time;
            str += " </font></b>";
            str += getString(R.string.min2);
        }

        ((TextView) findViewById(R.id.main_reminder)).setText(Html.fromHtml(str));
    }

    /**
     * Receives the index of a beed and returns the associated time. In case no beed was selected then it returns "0"
     *
     * @param beedIndex
     * @return
     */
    @SuppressWarnings("JavaDoc")
    public static String getTimeFromBeedIndex(int beedIndex) {
        String time;
        switch (beedIndex) {
            case 1:
                time = "10";
                break;
            case 2:
                time = "15";
                break;
            case 3:
                time = "20";
                break;
            case 4:
                time = "25";
                break;
            case 5:
                time = "30";
                break;
            default:
                time = "0"; // same as "case -1"
                break;
        }

        return time;
    }

    /**
     * Check to see if entered values are ok and start the payment activity
     */
    private void startPaymentProcess() {
        Cars cars = Tools.getCars(ActivityMain.this); // function to get cars from preferences
        Calendar calendar = Calendar.getInstance();
        hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minOfDay = calendar.get(Calendar.MINUTE);
        LogService.log(TAG, "hourOfDay : " + hourOfDay + ": " + minOfDay);


        if (hourOfDay >= (PersistentUtil.getStartInterval(ActivityMain.this) / 60) && hourOfDay < (PersistentUtil.getStopInterval(ActivityMain.this) / 60)) {
            // enters here if when you start your parking you are in the correct time interval (parking is opened)
            LogService.log(TAG, "in of the schedule!!!!!");
            // Get the parking time
            if (hoursInput != null) {
                hours = Integer.valueOf(hoursInput.getText().toString());
                minutes = Integer.valueOf(minutesInput.getText().toString());
            }
            LogService.log(TAG, "hours and minutes :: " + hours + "::" + minutes);
            // gets time in minutes
            periodInMinutes = Tools.getMinutes(hours, minutes);
            // gets time until it will stop
            int timetoStop = PersistentUtil.getStopInterval(ActivityMain.this) - (hourOfDay * 60 + minOfDay);
            // gets time to start of next day
            int toIntervalStart = 24 - ((PersistentUtil.getStopInterval(ActivityMain.this) / 60) + (PersistentUtil.getStartInterval(ActivityMain.this) / 60));
            LogService.log(TAG, "to interval stop: " + toIntervalStart);
            // if parking fee stops (after interval stop) it will add the hours from IntervalStop to Interval start as free
            if (timetoStop < periodInMinutes) {
                periodInMinutes = periodInMinutes + toIntervalStart * 60;
            }
            LogService.log(TAG, "time to count after its modified: " + periodInMinutes);
            calculateCost(cars);

        } else {
            Toast.makeText(ActivityMain.this, ActivityMain.this.getResources().getString(R.string.extended_until_parking) + (PersistentUtil.getStartInterval(ActivityMain.this) / 60), Toast.LENGTH_SHORT).show();
            // enters here if parking is closed when starting parking
            LogService.log(TAG, "out of the schedule!!!!!");
            if (hoursInput != null) {
                hours = Integer.valueOf(hoursInput.getText().toString());
                minutes = Integer.valueOf(minutesInput.getText().toString());
            }
            LogService.log(TAG, "hours and minutes :: " + hours + "::" + minutes);

            periodInMinutes = Tools.getMinutes(hours, minutes);
            if (hourOfDay < (PersistentUtil.getStartInterval(ActivityMain.this) / 60)) {
                LogService.log(TAG, "hours of day: " + hourOfDay + "get start interval: " + PersistentUtil.getStartInterval(ActivityMain.this));
                int timeto0 = PersistentUtil.getStartInterval(ActivityMain.this) - (hourOfDay * 60 + minOfDay); // will get the time that needs to pass untill the parking opens
                LogService.log(TAG, "to interval stop: " + timeto0);
                // sets the time as the selected time + time to next day morning
                periodInMinutes = periodInMinutes + timeto0;
                LogService.log(TAG, "time to count after its modified: " + periodInMinutes);
                calculateCost(cars);
            } else {
                LogService.log(TAG, "hours of day: " + hourOfDay + "get start interval: " + hours);
                // gets time to next day morning
                int timeto0 = 24 * 60 - (hourOfDay * 60 + minOfDay) + PersistentUtil.getStartInterval(ActivityMain.this);
                LogService.log(TAG, "to interval stop: " + timeto0);
                // sets the time as the selected time + time to next day morning
                periodInMinutes = periodInMinutes + timeto0;
                LogService.log(TAG, "time to count after its modified: " + periodInMinutes);
                calculateCost(cars);
            }
        }

    }

    /**
     * This method will navigate us to the payment screen
     *
     * @param cars
     * @param cost
     */
    @SuppressWarnings("JavaDoc")
    private void startPaymentIntent(Cars cars, String cost) { // will make intent to the payment activity
        if (cost.equalsIgnoreCase("0")) {
            startFreeParking();
        } else {
            Intent intent = new Intent(this, ActivityPayment.class);
            intent.putExtra("timeToCount", Tools.getSeconds(0, min));
            intent.putExtra("reminder", Tools.getSeconds(0, reminder));
            intent.putExtra("extended", false);
            intent.putExtra("cost", cost);
            intent.putExtra("selectedLocationPosition", selectedLocationAreaId);
            intent.putExtra("maxParkingHours", selectedLocationData.getMaxParkingHours());

            // sets the Persistent selected car to the needed car.

            for (Car car : cars) {
                if (car.getCnp().contentEquals(PersistentUtil.getSelectedCar(ActivityMain.this))) {
                    car.setTimeToCount(Tools.getSeconds(0, periodInMinutes));
                    car.setParkedLocationId(selectedLocationAreaId);
                    if (PersistentUtil.getBoolean(this, PersistentUtil.REMINDER_ON)) {
                        Intent intentPending = new Intent(ActivityMain.this, AlarmReceiver.class);
                        intentPending.setAction("intent_myaction_alarm");
                        pendingIntent = PendingIntent.getBroadcast(ActivityMain.this, (int) car.getExactTime(), intentPending, 0);
                        alarmManager = (AlarmManager) ActivityMain.this.getSystemService(ActivityMain.ALARM_SERVICE);
                        if (alarmManager != null) {
                            alarmManager.cancel(pendingIntent);
                        }
                    }
                    break;
                }
                LogService.log(TAG, "car is: " + car.toString());
            }
            cars = Tools.getCars(ActivityMain.this);
            Cars cars2 = new Cars();
            for (Car car : cars) {
                final float begin = (System.currentTimeMillis() - car.getExactTime());
                float time = ((car.getTimeToCount() * 1000) - begin);
                if (car.getTimeToCount() != -1 && time > -1) {
                    cars2.add(car);
                }
            }
            // sets where to go when you start app
            if (cars2.size() + 1 < 1) {
                PersistentUtil.setMainPageForBack(ActivityMain.this, PersistentUtil.ACTIVITY_MAIN);
            } else if (cars2.size() + 1 != 1) {
                PersistentUtil.setMainPageForBack(ActivityMain.this, PersistentUtil.ACTIVITY_MY_CARS_LIST);
            } else {
                PersistentUtil.setMainPageForBack(ActivityMain.this, PersistentUtil.ACTIVITY_REMAINING_TIME);
            }
            startActivityForResult(intent, Constants.ACTIVITY_PAYMENT);
        }
    }

    /**
     * Checks to see if the user selected all required information for parking spot
     *
     * @return
     */
    @SuppressWarnings("JavaDoc")
    private boolean inputDataIsValid() {

        // The parking time needs to be at least 10 minutes
        LogService.log(TAG, "PersistentUtil.getSelectedCar(ActivityMain.this)" + PersistentUtil.getSelectedCar(ActivityMain.this));
        if (hoursInput != null) {
            LogService.log(TAG, "hours input: " + hoursInput.getText().toString());
            hours = Integer.valueOf(hoursInput.getText().toString());
            minutes = Integer.valueOf(minutesInput.getText().toString());
            if (hours == 0 && minutes == 0) {
                Tools.showInfoDialog(this, getString(R.string.main_validation_parking_time_title), getString(R.string.main_validation_parking_time_body));
                return false;
            }
            if (Tools.getMinutes(hours, minutes) < 5) {
                Tools.showInfoDialog(this, getString(R.string.main_validation_parking_time_title), getString(R.string.main_validation_parking_time_body));
                return false;
            }
            if (PersistentUtil.getSelectedCar(ActivityMain.this).contentEquals("-1")) {
                Tools.showInfoDialog(this, getString(R.string.main_validation_parking_time_title), getString(R.string.main_validation_parking_time_body_spot));
                return false;
            }

        } else {
            if (PersistentUtil.getSelectedCar(ActivityMain.this).contentEquals("-1")) {
                Tools.showInfoDialog(this, getString(R.string.main_validation_parking_time_title), getString(R.string.main_validation_parking_time_body_spot));
                return false;
            } else {
                return true;
            }
        }
        return true;
    }

    /**
     * calculate cost for the selected time interval
     *
     * @param cars
     * @return
     */
    @SuppressWarnings("JavaDoc")
    private void calculateCost(Cars cars) {
        // calculates the cost of the parking
        Calendar calendar = Calendar.getInstance();
        hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minOfDay = calendar.get(Calendar.MINUTE);
        LogService.log(TAG, "hourOfDay : " + hourOfDay + ": " + minOfDay);

        // if maximum hours selected, minutes will be set on 0
        if (hours == maxParkingHours && minutesNP != null) {
            minutes = 0;
            minutesNP.setValue(minutesNP.getMinValue());
        }
        min = Tools.getMinutes(hours, minutes);

        PersistentUtil.setInt(this, min, "selected_parking_period");

        double chf;

        int freeTime = PersistentUtil.getFreeParking(ActivityMain.this);
        if (freeTime > 0 && freeTime < min) {
            // this is the method that checks for the parking Charges that we get from the WS and applies them
            calculateFinalCost(min, cars);
            // chf = Tools.getFinalCost(ActivityMain.this, min, taxes);//calculateFinalCost(min);
        } else if (freeTime > 0 && freeTime >= min) {
            chf = 0;
            showFinalCost(chf);

        } else {
            // this is the method that checks for the parking Charges that we get from the WS and applies them
            calculateFinalCost(min, cars);
            // chf = Tools.getFinalCost(ActivityMain.this, min, taxes);//calculateFinalCost(min);

        }
        // gets convenience fee and adds it to the cost
    }

    private void showFinalCost(double chf) {
        String cost = "<b>";
        findViewById(R.id.main_cost_value).setVisibility(View.VISIBLE);
        DecimalFormat df = new DecimalFormat("0.00");

        if (locationData.getConvenienceFee().getConvenienceFeeAmount() != null) {
            chf += Double.valueOf(locationData.getConvenienceFee().getConvenienceFeeAmount().replace(",", "."));
        } else {
            Toast.makeText(ActivityMain.this, "Error parsing convenience fee!!", Toast.LENGTH_SHORT).show();
        }
        String currency = PersistentUtil.getCurrency(ActivityMain.this); // gets type of currency we use
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
        int selectedParkingPeriod = PersistentUtil.getInt(this, "selected_parking_period");
        if (selectedParkingPeriod != -1) {
            numberPickerTv.setText(selectedParkingPeriod / 60 + " " + getString(R.string.main_parking_time_hour_unit) + " : " + selectedParkingPeriod % 60 + " " + getString(R.string.main_parking_time_minute_unit) + " ");
        } else {
            numberPickerTv.setText(hours + " " + getString(R.string.main_parking_time_hour_unit) + " : " + minutes + " " + getString(R.string.main_parking_time_minute_unit) + " ");
        }

    }


    /**
     * Call computed tariff call, which returns the tariff for the selected time
     *
     * @param min
     * @param cars
     * @return
     */
    @SuppressWarnings("JavaDoc")
    private void calculateFinalCost(final int min, final Cars cars) {
        // if application first install, and there is no car number plate introduced hide cost value
        // we call computedCost method only to update parking period and some other stuff
        if (car.equalsIgnoreCase("-1")) {
            computedCost(cars, min, "0");
            findViewById(R.id.main_cost_value).setVisibility(View.GONE);
        } else {
            WSCalls.callComputedTariff(loader, this, car, selectedLocationAreaId, min, new GetBackFromWS() {

                @Override
                public void success(ArrayList<Taxes> taxes) {

                }

                @Override
                public void success() {
                    if(PersistentUtil.getBoolean(ActivityMain.this, PersistentUtil.WHITE_LIST_CNP)){
                        numberPickerTv.setClickable(false);
                    }else{
                        numberPickerTv.setClickable(true);
                    }
                    computedCost(cars, Integer.valueOf(PersistentUtil.getString(ActivityMain.this, PersistentUtil.PARKING_DURATION_MINUTES)), PersistentUtil.getString(ActivityMain.this, PersistentUtil.FREE_PARKING_DURATION_MINUTES));
                }

                @Override
                public void fail(Integer Err) {

                }
            });
        }
    }

    private void computedCost(Cars cars, int min, String freeParkingDuration) {

        if (!freeParkingDuration.equalsIgnoreCase("0")) {
            Toast.makeText(this, String.format(getString(R.string.free_parking_duration_in_minutes_toast), freeParkingDuration), Toast.LENGTH_SHORT).show();
        }

        String finalCost = PersistentUtil.getString(this, PersistentUtil.COMPUTED_TARIFF);
        double costValue;

        if (finalCost != null && !finalCost.equalsIgnoreCase("")) {
            // handle back-end response for cost value with , and .
            costValue = Double.valueOf(finalCost.contains(",") ? finalCost.replace(",", ".") : finalCost) / 100;
        } else {
            costValue = 0.0;
        }

        if (cars != null) {
            startPaymentIntent(cars, String.valueOf(costValue));
        } else {
//            PersistentUtil.setInt(this, Tools.getMinutes(min / 60, 0), "selected_parking_period");
            PersistentUtil.setBoolean(this, false, PersistentUtil.HAS_SPECIAL_TIME_LIMIT);
            showFinalCost(costValue);
        }
    }


    private void callDialog() {
        // dialog for the number pickers
        cars = Tools.getCars(ActivityMain.this);
        if (dialogShouldCall) {
            final Dialog dialog = new Dialog(ActivityMain.this);
            dialog.setContentView(R.layout.np_dialog);
            dialog.setTitle(getString(R.string.select_time));
            NumberPicker hoursNP = (NumberPicker) dialog.findViewById(R.id.hour_number_picker);
            minutesNP = (NumberPicker) dialog.findViewById(R.id.min_number_picker);
            LinearLayout numberpicker = (LinearLayout) dialog.findViewById(R.id.np_layout);
            numberpicker.getLayoutParams().height = (int) (160 * getResources().getDisplayMetrics().density);

            Tools.initializeNumberPicker(hoursNP, 0, selectedLocationData.getMaxParkingHours(), 1, 1);
            Tools.initializeNumberPickerMinutes(minutesNP, 0, 61, PersistentUtil.getStepRate(ActivityMain.this));
            hoursNP.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                }

            });
            minutesNP.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
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
                    if (hoursInput != null && !hoursInput.getText().toString().equalsIgnoreCase("")) {
                        hours = Integer.valueOf(hoursInput.getText().toString());
                        minutes = Integer.valueOf(minutesInput.getText().toString());
                    } else {
                        hours = 1;
                        minutes = 0;
                    }
                    int inMinutes = Tools.getMinutes(hours, minutes);
                    PersistentUtil.setInt(ActivityMain.this, Tools.getMinutes(hours, minutes), "selected_parking_period");
                    calculateCost(null);
                    dialog.dismiss();
                }
            });

            dialog.show();
        }/*
         * else{ Toast.makeText(ActivityMain.this, "Only all day parking available", Toast.LENGTH_SHORT).show(); }
		 */

    }

    private void displayHelpOverlay(boolean forceShow) {

        // Get the value from shared prefs
        boolean neverShowAgain = RoboDemo.isNeverShowAgain(this, KEY_HELP_OVERLAY);

        if (forceShow || !neverShowAgain) {

            ArrayList<LabeledPoint> arrayListPoints = new ArrayList<LabeledPoint>();

			/*
             * Create a list of LabeledPoints
			 */
            LabeledPoint p;

            // Watch
            p = new LabeledPoint(this, 0.40f, 0.30f, "Step 1: " + getString(R.string.add_car));
            arrayListPoints.add(p);
            // Gallery
            p = new LabeledPoint(this, 0.40f, 0.60f, "Step 2: " + getString(R.string.select_time));
            arrayListPoints.add(p);
            // Toggle menu icon
            p = new LabeledPoint(this, 0.40f, 0.90f, "Step 3: " + getString(R.string.main_start_parking));
            arrayListPoints.add(p);

            // Start the activity
            Intent intent = new Intent(this, ActivityHelpOverlay.class);
            if (forceShow) {
                RoboDemo.prepareDemoActivityIntent(intent, KEY_HELP_OVERLAY, arrayListPoints);
            } else {
                RoboDemo.prepareDemoActivityIntent(intent, KEY_HELP_OVERLAY, arrayListPoints);
            }
            startActivity(intent);
        }
    }

    /**
     * Show and hide wheel picker on edittext focus changing
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.location_name:
                if (!hasFocus) {
                    pickerContainer.setVisibility(View.GONE);
                }
                break;
            default:
        }
    }

    /**
     * before start parking this method is checking if the selected car has any free time parking index shows if the method is called for free parking or for a longer time
     *
     * @param index
     * @param carNumber
     * @param getBackFromConfirmation
     */
    @SuppressWarnings("JavaDoc")
    private void getFreeParkingAvailable(final int index, final String carNumber, final getBackFromConfirmation getBackFromConfirmation) {
        boolean forFreeParking = false;
        switch (index) {
            case 0:
                forFreeParking = true;
                break;
            case 1:
                forFreeParking = false;
                break;

            default:
        }

        WSCalls.callFreeParkingAvailable(loader, this, carNumber, selectedLocationAreaId, forFreeParking, new GetBackFromWS() {

            @Override
            public void success(ArrayList<Taxes> taxes) {
            }

            @Override
            public void success() {
                if (index == 1) {
                    startPaymentProcess();
                } else if (index == 0) {
                    if (PersistentUtil.getFreeParking(ActivityMain.this) > 0) {
                        startFreeParking();
                        getBackFromConfirmation.newParking();
                    } else {
                        getBackFromConfirmation.newParking();
                        Toast.makeText(ActivityMain.this, getResources().getString(R.string.freetime_used), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void fail(Integer Err) {
                // getFreeParkingAvailable(index, carNumber);
                Toast.makeText(ActivityMain.this, getResources().getString(R.string.freetime_used), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void startFreeParking() {
        periodInMinutes = Tools.getMinutes(0, PersistentUtil.getFreeParking(ActivityMain.this));
        // Set the flag to indicate the timer has been started
        PersistentUtil.setTimerStarted(ActivityMain.this, true);

        // Start the timer activity and send these values
        Intent intent = new Intent(ActivityMain.this, ActivityRemainingTime.class);
        // Make all activities on top of ActivityRemainingTime close
        intent.putExtra("timeToCount", Tools.getSeconds(0, periodInMinutes));
        intent.putExtra("reminder", Tools.getSeconds(0, reminder));
        intent.putExtra("successMessage", false);
        intent.putExtra("selectedLocationPosition", selectedLocationAreaId);

        ActivityMain.this.startActivity(intent);

        ActivityMain.this.setResult(Constants.CLOSE_ACTIVITY);
        ActivityMain.this.finish();
    }
}
