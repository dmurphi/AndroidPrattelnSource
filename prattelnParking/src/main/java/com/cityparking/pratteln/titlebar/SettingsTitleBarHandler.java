package com.cityparking.pratteln.titlebar;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.cityparking.pratteln.R;
import com.cityparking.pratteln.activities.ActivityReminder;
import com.cityparking.pratteln.fragments.InvoiceFragment;
import com.cityparking.pratteln.fragments.ProfileFragment;
import com.cityparking.pratteln.fragments.ReminderFragment;
import com.cityparking.pratteln.fragments.TermsFragment;
import com.cityparking.pratteln.utils.LogService;

public class SettingsTitleBarHandler {

	private static final String TAG = TitleBarHandler.class.getSimpleName();

    /**
     * Initializes the title bar by either hiding or displaying parts of it, setting event listeners, etc
     *
     * @param context
     * @param title_text
     * @param showBack
     * @param fragmentClass
     */
    @SuppressWarnings("JavaDoc")
	public static void initTitleBar(final Context context, String title_text, boolean showBack, Class<?> fragmentClass) {
		try {
			if (context instanceof Activity) {

				// Get the references for the views
				TextView title_tv = ((TextView) ((Activity) context).findViewById(R.id.reusable_title_text));
				TextView back_tv = ((TextView) ((Activity) context).findViewById(R.id.reusable_title_back_tv));
				TextView settings_tv = ((TextView) ((Activity) context).findViewById(R.id.reusable_title_settings));

				// Initialize the title text
				if (title_text != null) {
					// If the title contains an "|" character then it means it is a "city|country" string so we need to make the country green
					if (!title_text.contains("|")) {
						title_tv.setText(title_text);
					} else {
						title_tv.setText(Html.fromHtml(makeTitleTextGreen(title_text)));
					}
				}

				// Initialize the settings icon
				if (fragmentClass.equals(TermsFragment.class)) {
					LogService.log(TAG, "terms fragment");
					settings_tv.setVisibility(View.GONE);
				} else if (fragmentClass.equals(ProfileFragment.class)) {
					LogService.log(TAG, "profile fragment");
					settings_tv.setVisibility(View.VISIBLE);
					settings_tv.setText(context.getString(R.string.time_new_parking_save));
					settings_tv.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							((ActivityReminder) context).saveProfileData();
						}
					});
				} else if (fragmentClass.equals(ReminderFragment.class)) {
					LogService.log(TAG, "reminder fragment");
					settings_tv.setVisibility(View.GONE);
				} else if (fragmentClass.equals(InvoiceFragment.class)) {
					LogService.log(TAG, "invoice fragment");
					settings_tv.setVisibility(View.VISIBLE);
					settings_tv.setText(context.getString(R.string.send));
					settings_tv.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							((ActivityReminder) context).sendInvoice();
						}
					});
				}

				// Initialize the back button
				if (showBack) {

					back_tv.setVisibility(View.VISIBLE);

					back_tv.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// context.startActivity(new Intent(context, backActivity));
							((Activity) context).finish();
						}
					});
				}

			} else {
				LogService.errline(TAG, "context is not an Activity");
			}
		} catch (NullPointerException e) {
			LogService.err(TAG, "Null pointer exception on reusable title. Have you included the reusable title bar in your layout ?", e);
		}
	}

	/**
	 * If the title text has "city|country" format then we need to change the color of the country
	 * 
	 * @param title
	 */
    @SuppressWarnings("JavaDoc")
	private static String makeTitleTextGreen(String title) {

		LogService.log(TAG, "makeTitleGreen : title : " + title);

		// If we can't find a "|" then just return the original string
		if (!title.contains("|")) {
			return title;
		}

		// Split the text on "|"
		String[] separated = title.split("\\|");

		// In case it didn't separate them correctly then something went wrong so return original text
		if (separated.length != 2)
			return title;

		// Concatenate the new string with color
		String str = separated[0];
		str += "<font color=\"#c0d877\">";
		str += "|";
		str += separated[1];
		str += "</font>";

		LogService.log(TAG, "makeTitleGreen : title[0] : " + separated[0] + " : title[1] : " + separated[1] + ": str : " + str);

		// Set the title text
		return str;
	}
}
