package com.cityparking.pratteln.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.cityparking.pratteln.entities.LastParking;
import com.cityparking.pratteln.webservices.entity.LocationGlobalDataResponseBody;
import com.cityparking.pratteln.webservices.entity.ResidentTariff;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PersistentUtil {

    /*****************
     * KEYS
     ************************************/

    private static final String TIMER_STARTED = "timer_started";

    private static final String SELECTED_BEED = "selected_beed";

    private static final String SELECTED_CAR = "selected_car";

    private static final String PARKING_SPOTS = "parking_spots";

    private static final String CARS = "cars";

    private static final String PROFILE = "profile";

    private static final String MAIN_PAGE_FOR_BACK = "mainpageforback";

    private static final String CARD_NR = "card_nr";

    private static final String FIRST_RUN = "first_run";

    public static final int ACTIVITY_MAIN = 0;

    public static final int ACTIVITY_REMAINING_TIME = 1;

    public static final int ACTIVITY_MY_CARS_LIST = 2;

    private static final String INTERVAL_START = "start_int";

    private static final String INTERVAL_STOP = "stop_int";

    private static final String MAX_HOURS = "max_hours";

    private static final String CONVENIENCE_FEE = "convenience_fee";

    private static final String CURRENCY = "currency";

    private static final String SHOW_HELP = "show_help";

    private static final String FREE_PARKING_AVAILABLE = "free_parking";

    private static final String FREE_PARKING_DIRECTIVE_ID = "directive_id";

    private static final String ALL_DAY_PARKING = "all_day_paring";

    private static final String STEP_RATE = "step_rate";

    public static final String SPECIAL_RESIDENT = "special_resident";

    // parameters of computed tariff call
    public static final String PREVIOUS_PARKING = "previous_parking";

    public static final String COMPUTED_TARIFF = "computed_tariff";

    public static final String HAS_SPECIAL_TIME_LIMIT = "has_special_time_limit";

    public static final String SPECIAL_TIME_LIMIT = "special_time_limit";

    public static final String REMINDER_ON = "reminder_on";

    public static final String PARKING_DURATION_MINUTES = "parking_duration_minutes";
    public static final String FREE_PARKING_DURATION_MINUTES = "free_parking_duration_minutes";
    public static final String WHITE_LIST_CNP = "white_list_cnp";

    public static LocationGlobalDataResponseBody PARKING_LOCATION = new LocationGlobalDataResponseBody();
    public static ResidentTariff RESIDENT_LOCATION_TARIFF = new ResidentTariff();


    /*****************
     * obtainSharedPreferences
     ************************************/

    private static SharedPreferences obtainSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Just so we don't have to call getSharedPRegerences and e.commit every time
     *
     * @param context
     * @param strValue
     * @param prefKey
     * @return
     */
    @SuppressWarnings("JavaDoc")
    public static boolean setString(Context context, String strValue, String prefKey) {
        Editor e = PreferenceManager.getDefaultSharedPreferences(context).edit();
        e.putString(prefKey, strValue);
        return e.commit();
    }

    /**
     * Just so we don't have to call getSharedPRegerences and e.commit every time
     *
     * @param context
     * @param prefKey
     * @return
     */
    @SuppressWarnings("JavaDoc")
    public static String getString(Context context, String prefKey) {
        return obtainSharedPreferences(context).getString(prefKey, "");
    }

    /**
     * Just so we don't have to call getSharedPRegerences and e.commit every time
     *
     * @param context
     * @param boolValue
     * @param prefKey
     * @return
     */
    @SuppressWarnings("JavaDoc")
    public static boolean setBoolean(Context context, boolean boolValue, String prefKey) {
        Editor e = PreferenceManager.getDefaultSharedPreferences(context).edit();
        e.putBoolean(prefKey, boolValue);
        return e.commit();
    }

    /**
     * Just so we don't have to call getSharedPRegerences and e.commit every time
     *
     * @param context
     * @param intValue
     * @param prefKey
     * @return
     */
    @SuppressWarnings("JavaDoc")
    public static boolean setInt(Context context, int intValue, String prefKey) {
        Editor e = PreferenceManager.getDefaultSharedPreferences(context).edit();
        e.putInt(prefKey, intValue);
        return e.commit();
    }

    /**
     * Just so we don't have to call getSharedPRegerences and e.commit every time
     *
     * @param context
     * @param prefKey
     * @return
     */
    @SuppressWarnings("JavaDoc")
    public static int getInt(Context context, String prefKey) {
        return obtainSharedPreferences(context).getInt(prefKey, -1);
    }

    /**
     * Just so we don't have to call getSharedPRegerences and e.commit every time
     *
     * @param context
     * @param prefKey
     * @return
     */
    @SuppressWarnings("JavaDoc")
    public static boolean getBoolean(Context context, String prefKey) {
        return obtainSharedPreferences(context).getBoolean(prefKey, false);
    }

    /**
     * Just so we don't have to call getSharedPRegerences and e.commit every time
     *
     * @param context
     * @param longValue
     * @param prefKey
     * @return
     */
    @SuppressWarnings("JavaDoc, unused")
    private static boolean setLong(Context context, long longValue, String prefKey) {
        Editor e = PreferenceManager.getDefaultSharedPreferences(context).edit();
        e.putLong(prefKey, longValue);
        return e.commit();
    }

    /*****************
     * SETTERS (start)
     ************************************/

    public static boolean setTimerStarted(Context context, boolean timerStarted) {
        return setBoolean(context, timerStarted, TIMER_STARTED);
    }

    /**
     * The beed index starts from 1 and not 0 !!!!
     */
    public static boolean setSelectedBeed(Context context, int beedIndex) {
        return setInt(context, beedIndex, SELECTED_BEED);
    }

    public static boolean setFirstRun(Context context, boolean first) {
        return setBoolean(context, first, FIRST_RUN);
    }

    public static boolean setShowHelp(Context context, boolean help) {
        return setBoolean(context, help, SHOW_HELP);
    }

    public static boolean setSelectedCar(Context context, String string) {
        return setString(context, string, SELECTED_CAR);
    }

    @SuppressWarnings("unused")
    public static boolean setConvenienceFee(Context context, String string) {
        return setString(context, string, CONVENIENCE_FEE);
    }

    public static boolean setCurrency(Context context, String string) {
        return setString(context, string, CURRENCY);
    }

    public static boolean setMainPageForBack(Context context, int string) {
        return setInt(context, string, MAIN_PAGE_FOR_BACK);
    }

    public static boolean setStopInterval(Context context, int string) {
        return setInt(context, string, INTERVAL_STOP);
    }

    public static boolean setStartInterval(Context context, int string) {
        return setInt(context, string, INTERVAL_START);
    }

    @SuppressWarnings("unused")
    public static boolean setMaxHours(Context context, int string) {
        return setInt(context, string, MAX_HOURS);
    }

    public static boolean setProfile(Context context, String string) {
        return setString(context, string, PROFILE);
    }

    public static boolean setCars(Context context, String string) {
        return setString(context, string, CARS);
    }

    public static boolean setCardNr(Context context, String string) {
        return setString(context, string, CARD_NR);
    }

    @SuppressWarnings("unused")
    public static boolean setParkingSpots(Context context, int parkingspots) {
        return setInt(context, parkingspots, PARKING_SPOTS);
    }

    public static void setParkingLocation(LocationGlobalDataResponseBody location) {
        PARKING_LOCATION = location;
    }

    public static boolean setFreeParking(Context context, int free) {
        return setInt(context, free, FREE_PARKING_AVAILABLE);
    }

    public static boolean setDirectiveId(Context context, int directiveId) {
        return setInt(context, directiveId, FREE_PARKING_DIRECTIVE_ID);
    }

    @SuppressWarnings("unused")
    public static boolean setAllDayParking(Context context, int allDayCost) {
        return setInt(context, allDayCost, ALL_DAY_PARKING);
    }

    public static boolean setStepRate(Context context, int step) {
        return setInt(context, step, STEP_RATE);
    }

    public static boolean setResident(Context context, boolean resident) {
        return setBoolean(context, resident, SPECIAL_RESIDENT);
    }

    public static void setResidentLocation(ResidentTariff residentTariff) {
        RESIDENT_LOCATION_TARIFF = residentTariff;
    }

    /*****************
     * GETTERS (start)
     ************************************/
    public static boolean getTimerStarted(Context con) {
        return obtainSharedPreferences(con).getBoolean(TIMER_STARTED, false);
    }

    public static boolean getFirstRun(Context con) {
        return obtainSharedPreferences(con).getBoolean(FIRST_RUN, true);
    }

    public static boolean getShowHelp(Context con) {
        return obtainSharedPreferences(con).getBoolean(SHOW_HELP, false);
    }

    public static int getSelectedBeed(Context con) {
        return obtainSharedPreferences(con).getInt(SELECTED_BEED, -1);
    }

    public static String getSelectedCar(Context con) {
        return obtainSharedPreferences(con).getString(SELECTED_CAR, "-1");
    }

    @SuppressWarnings("unused")
    public static String getConvenienceFee(Context con) {
        return obtainSharedPreferences(con).getString(CONVENIENCE_FEE, "-1");
    }

    public static String getCurrency(Context con) {
        return obtainSharedPreferences(con).getString(CURRENCY, "-1");
    }

    public static int getMainPageForBack(Context con) {
        return obtainSharedPreferences(con).getInt(MAIN_PAGE_FOR_BACK, -1);
    }

    public static int getStopInterval(Context con) {
        return obtainSharedPreferences(con).getInt(INTERVAL_STOP, -1);
    }

    public static int getStartInterval(Context con) {
        return obtainSharedPreferences(con).getInt(INTERVAL_START, -1);
    }

    public static int getMaxHours(Context con) {
        return obtainSharedPreferences(con).getInt(MAX_HOURS, 13);
    }

    public static String getCardNr(Context con) {
        return obtainSharedPreferences(con).getString(CARD_NR, "");
    }

    public static String getCars(Context con) {
        return obtainSharedPreferences(con).getString(CARS, "");
    }

    public static String getProfile(Context con) {
        return obtainSharedPreferences(con).getString(PROFILE, "");
    }

    @SuppressWarnings("unused")
    public static int getParkingSpots(Context con) {
        return obtainSharedPreferences(con).getInt(PARKING_SPOTS, -1);
    }

    public static LocationGlobalDataResponseBody getParkingLocation() {
        return PARKING_LOCATION;
    }

    public static int getFreeParking(Context con) {
        return obtainSharedPreferences(con).getInt(FREE_PARKING_AVAILABLE, -1);
    }

    public static int getDirectiveId(Context con) {
        return obtainSharedPreferences(con).getInt(FREE_PARKING_DIRECTIVE_ID, -1);
    }

    public static int getAllDayCost(Context con) {
        return obtainSharedPreferences(con).getInt(ALL_DAY_PARKING, -1);
    }

    public static int getStepRate(Context con) {
        return obtainSharedPreferences(con).getInt(STEP_RATE, -1);
    }

    public static boolean getResident(Context con) {
        return obtainSharedPreferences(con).getBoolean(SPECIAL_RESIDENT, false);
    }

    public static ResidentTariff getResidentLocation() {
        return RESIDENT_LOCATION_TARIFF;
    }


    public static void savePreviousParking(Context context, ArrayList<LastParking> parkingEntityList) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        Set<String> set = new HashSet<String>();
        for (int i = 0; i < parkingEntityList.size(); i++) {
            set.add(parkingEntityList.get(i).getJSONObject().toString());
        }
        editor.putStringSet(PREVIOUS_PARKING, set);
        editor.commit();
    }

    public static void clearPreviousParkingList(Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.remove(PREVIOUS_PARKING);
        editor.commit();
    }

    public static ArrayList<LastParking> getPreviousParking(Context context) {
        ArrayList<LastParking> parkingEntityList = new ArrayList<LastParking>();

        Set<String> set = obtainSharedPreferences(context).getStringSet(PREVIOUS_PARKING, null);
        if (set != null && set.size() > 0) {
            for (String s : set) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String plateNumber = jsonObject.getString("plate_number");
                    long currentTime = jsonObject.getLong("current_time_in_millis");
                    String parkingDuration = jsonObject.getString("parking_duration");
                    String parkingCost = jsonObject.getString("parking_cost");
                    int maxParkingHour = jsonObject.getInt("max_parking_hour");
                    int locationAreaId = jsonObject.getInt("location_area_id");
                    LastParking parkingEntity = new LastParking(plateNumber, currentTime, parkingDuration, parkingCost, maxParkingHour, locationAreaId);
                    parkingEntityList.add(parkingEntity);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return parkingEntityList;
    }

}
