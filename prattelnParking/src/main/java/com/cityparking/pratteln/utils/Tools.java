package com.cityparking.pratteln.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.cityparking.pratteln.R;
import com.cityparking.pratteln.constants.Constants;
import com.cityparking.pratteln.entities.Cars;
import com.cityparking.pratteln.entities.Profile;
import com.cityparking.pratteln.entities.Taxes;
import com.google.gson.Gson;

import net.simonvt.numberpicker.NumberPicker;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Tools {

	public static final String TAG = Tools.class.getSimpleName();

	// Used in dialog which
	private static Activity pActivity;

	/**
	 * Checking for a valid network connection (if internet is available)
	 * @param context
	 * @return
     */
    @SuppressWarnings("JavaDoc")
    public static boolean networkIsAvailable(Context context) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

	/**
	 * Displays a info dialog with custom title and message
	 *
	 * @param parentActivity
	 * @param title
	 * @param message
	 */
    @SuppressWarnings("JavaDoc")
	public static void showInfoDialog(Context parentActivity, String title, String message) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
		builder.setTitle(title);
		builder.setMessage(message);

		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

				dialog.dismiss();
			}
		});

		if (Looper.myLooper() == Looper.getMainLooper()) {
			builder.show();
		} else {
			((Activity) parentActivity).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					try {
						builder.show();
					} catch (Exception e) {
						LogService.log(TAG, e.getMessage());
					}
				}
			});
		}
	}

	/**
	 * Displays a dialog asking the user if he wants to quick the app or not. Usually launched on Back button press. If yes, then finish current activity, if not dismiss the dialog.
	 *
	 * @param parentActivity
	 */
    @SuppressWarnings("JavaDoc")
	public static void showCloseAppConfirmation(Activity parentActivity) {

		Tools.pActivity = parentActivity;

		final AlertDialog.Builder builder = new AlertDialog.Builder(Tools.pActivity);
		builder.setTitle(Tools.pActivity.getString(R.string.close_app_title));
		builder.setMessage(Tools.pActivity.getString(R.string.close_app_body));

		builder.setPositiveButton(Tools.pActivity.getString(R.string.yes), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Tools.pActivity.finish();
			}
		});

		builder.setNegativeButton(Tools.pActivity.getString(R.string.cancel), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		if (Looper.myLooper() == Looper.getMainLooper()) {
			builder.show();

		}
	}

	/**
	 * Determines the width and height of the screen in the current orientation. This method will return switched values when orientation changes so recall it if necessary.
	 *
	 * @param context
	 * @return int[2] array with first element being the width of the screen in the current orientation and the second being the height of the screen in the current orientation
	 */
    @SuppressWarnings("JavaDoc")
	public static int[] getScreenDimensions(Activity context) {
		DisplayMetrics display = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(display);

		return (new int[] { display.widthPixels, display.heightPixels });
	}

	/**
	 * Converts hours and/or minutes into seconds
	 *
	 * @param hours
	 * @param minutes
	 * @return
	 */
    @SuppressWarnings("JavaDoc")
	public static long getSeconds(int hours, long minutes) {
		return hours * 60 * 60 + minutes * 60;
	}

	/**
	 * Converts hours and/or minutes into seconds
	 *
	 * @param hours
	 * @param minutes
	 * @return
	 */
    @SuppressWarnings("JavaDoc")
	public static int getSecondsMin(int hours, int minutes) {
		return hours * 60 * 60 + minutes * 60;
	}

	/**
	 * Converts hours and/or minutes into minutes
	 * 
	 * @param hours
	 * @param minutes
	 * @return
	 */
    @SuppressWarnings("JavaDoc")
	public static int getMinutes(int hours, int minutes) {
		return hours * 60 + minutes;
	}

	/**
	 * Creates the md5 hash code for a specified string
	 * 
	 * @param src
	 * @return
	 */
    @SuppressWarnings("JavaDoc")
	public static String md5(String src) {

		String keyString = Constants.FINGERPRINT_KEY;
		keyString = keyString.substring(212, 244);
		String type = "HmacMD5";

		String digest = null;
		try {
			Mac mac = Mac.getInstance(type);
			SecretKeySpec key = new SecretKeySpec((keyString).getBytes("UTF-8"), mac.getAlgorithm());
			mac.init(key);

			byte[] bytes = mac.doFinal(src.getBytes("ASCII"));

			StringBuilder hash = new StringBuilder();
			for (byte b : bytes) {
				String hex = Integer.toHexString(0xFF & b);
				if (hex.length() == 1) {
					hash.append('0');
				}
				hash.append(hex);
			}
			digest = hash.toString();
		} catch (UnsupportedEncodingException e) {
			LogService.log(TAG, "Exception occurred: " + e.getMessage());
		} catch (InvalidKeyException e) {
			LogService.log(TAG, "Exception occurred: " + e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			LogService.log(TAG, "Exception occurred: " + e.getMessage());
		}
		return digest;
	}


	/**
	 * Initialize a number_picker with default values.
	 * 
	 * @param picker
	 * @param min
	 * @param max
	 * @param increment
	 */
    @SuppressWarnings("JavaDoc")
	public static void initializeNumberPicker(NumberPicker picker, int min, int max, int increment, int hours) {
		if (picker != null) {

			// Calculate number of values to display
			int nrValues = ((max - min) / increment + 1);

			// Instantiate an empty array of strings
			String[] displayedValues = new String[nrValues];

			// Calculate the values of the array starting from "min" to "max" with increment "increment"
			displayedValues[displayedValues.length - 1] = "0";
			for (int i = 1; i < nrValues; i++) {
				displayedValues[i - 1] = String.valueOf(min + i * increment);
			}

			picker.setMinValue(0);
			picker.setMaxValue(displayedValues.length - 1);
			picker.setValue(hours - 1);
			picker.setDisplayedValues(displayedValues);
		} else {
			LogService.errline(TAG, "Number Picker is null for some reason");
		}
	}

	public static void initializeNumberPickerMinutes(NumberPicker picker, int min, int max, int increment) {
		if (picker != null) {

			// Calculate number of values to display
			int nrValues = ((max - min) / increment);

			// Instantiate an empty array of strings
			String[] displayedValues = new String[nrValues];

			// Calculate the values of the array starting from "min" to "max" with increment "increment"
			displayedValues[displayedValues.length - 1] = "0";
			for (int i = 0; i < nrValues; i++) {
				if(i == 0){
					displayedValues[0] = String.valueOf(min);
				}else {
					displayedValues[i] = String.valueOf(min + i * increment);
				}
				// LogService.log(TAG, "Picker displayed values: " + displayedValues[i - 1]);
			}

			// Invert the array so by scrolling upwards on the number picker it will scroll to higher numbers
			// Collections.reverse(Arrays.asList(displayedValues));

			picker.setMinValue(0);
			picker.setMaxValue(displayedValues.length - 1);
			picker.setDisplayedValues(displayedValues);
		} else {
			LogService.errline(TAG, "Number Picker is null for some reason");
		}
	}

	public static EditText findInput(ViewGroup np) {
		int count = np.getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = np.getChildAt(i);
			if (child instanceof ViewGroup) {
				findInput((ViewGroup) child);
			} else if (child instanceof EditText) {
				return (EditText) child;
			}
		}
		return null;
	}

	public static UUID generateGuid() { // will generate a random uuid
		return UUID.randomUUID();
	}

	public static void setCars(Cars cars, Context context) { // function to set cars for preferences
		Gson gson = new Gson();
		String json = gson.toJson(cars);
		LogService.log(TAG, "gson is: " + json);
		PersistentUtil.setCars(context, json);
	}

	public static Cars getCars(Context context) { // function to get cars from preferences
		String carsString = PersistentUtil.getCars(context);
		Cars cars;
		if (carsString.contentEquals("")) {
			cars = new Cars();
		} else {
			Gson gson = new Gson();
			cars = gson.fromJson(carsString, Cars.class);
		}
		return cars;
	}

	public static Profile getProfile(Context context) { // function to set profile for preferences
		String profString = PersistentUtil.getProfile(context);
		Profile profile;
		if (profString.contentEquals("")) {
			profile = new Profile();
		} else {
			Gson gson = new Gson();
			profile = gson.fromJson(profString, Profile.class);
		}
		return profile;
	}

	public static void setProfile(Profile profile, Context context) { // function to get profile from preferences
		Gson gson = new Gson();
		String json = gson.toJson(profile);
		LogService.log(TAG, "gson is: " + json);
		PersistentUtil.setProfile(context, json);
	}

	public static String inputIsValid(Context context, String first_name, String last_name, String email) { // will check if input from ProfileFragment is correct
		if (first_name.trim().equals("")) {
			return context.getString(R.string.firstname_null);
		}
		if (last_name.trim().equals("")) {
			return context.getString(R.string.lastname_null);
		}
		if (email.trim().equals("")) {
			return context.getString(R.string.email_null);
		}
		if (!email.contains("@")) {
			return context.getString(R.string.email_invalid);
		}
		return "ready";
	}

	public static String getDate(long timeStamp) { // will get the date from a timestamp

		try {
			DateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
			Date netDate = (new Date(timeStamp));
			return sdf.format(netDate);
		} catch (Exception ex) {
			return "xx";
		}
	}

	public static double getFinalCost(Context ctx, int min, ArrayList<Taxes> taxes) {
		int stepRate = PersistentUtil.getStepRate(ctx);
		double chf;
		if (taxes.size() > 1) {
			if (min > taxes.get(0).getMinutes()) {
				chf = taxes.get(0).getMinutes() * (float) (taxes.get(0).getPrice() / stepRate);
				Double minus = taxes.get(0).getMinutes();
				for (int i = 1; i < taxes.size(); i++) {
					if (min > taxes.get(i).getMinutes()) {
						chf += ((taxes.get(i).getMinutes() - minus) * (float) (taxes.get(i).getPrice() / stepRate));
						minus = taxes.get(i).getMinutes();
					} else {
						chf += ((min - minus) * (float) (taxes.get(i).getPrice() / stepRate));
						break;
					}
				}
			} else {
				chf = min * (float) (taxes.get(0).getPrice() / stepRate);
			}
		} else {
			if (taxes.size() > 0) {
				chf = min * (float) (taxes.get(0).getPrice() / stepRate);
			} else {
				chf = 0;
			}
		}
		return chf;
	}

}
