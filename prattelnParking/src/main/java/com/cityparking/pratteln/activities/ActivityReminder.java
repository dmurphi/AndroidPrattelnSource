package com.cityparking.pratteln.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cityparking.pratteln.R;
import com.cityparking.pratteln.entities.Profile;
import com.cityparking.pratteln.entities.Taxes;
import com.cityparking.pratteln.fragments.InvoiceFragment;
import com.cityparking.pratteln.fragments.ProfileFragment;
import com.cityparking.pratteln.fragments.ReminderFragment;
import com.cityparking.pratteln.fragments.TermsFragment;
import com.cityparking.pratteln.listeners.GetBackFromWS;
import com.cityparking.pratteln.titlebar.SettingsTitleBarHandler;
import com.cityparking.pratteln.utils.LogService;
import com.cityparking.pratteln.utils.PersistentUtil;
import com.cityparking.pratteln.utils.Tools;
import com.cityparking.pratteln.webservices.WSCalls;

import java.util.ArrayList;

public class ActivityReminder extends FragmentActivity implements OnClickListener {

	private static final String TAG = ActivityReminder.class.getSimpleName();

	private Fragment f;
    private Profile profile;

    private Button reminder, myProfile, history, terms, instructions;
    private Integer TAB_MY_PROFILE = 1, TAB_REMINDER = 2, TAB_HISTORY = 3, TAB_TERMS = 5;
    private String first_name, last_name, email;

	public boolean canChangeFragment = true;

	public RelativeLayout loader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_reminder);
		initFragmentManager();

	}

	private void initFragmentManager() {
		loader = (RelativeLayout) findViewById(R.id.splash_screen_loader_container);
		f = new ProfileFragment();
		// Initialize the title bar
		SettingsTitleBarHandler.initTitleBar(this, getString(R.string.location_settings), true, f.getClass());
		reminder = (Button) findViewById(R.id.footer_reminder);
		reminder.setOnClickListener(this);
		myProfile = (Button) findViewById(R.id.footer_myprofile);
		myProfile.setOnClickListener(this);
		history = (Button) findViewById(R.id.footer_history);
		history.setOnClickListener(this);
		instructions = (Button) findViewById(R.id.footer_instructions);
		instructions.setOnClickListener(this);
		terms = (Button) findViewById(R.id.footer_terms);
		terms.setOnClickListener(this);
		changeFragment(f.getClass());
	}

	@Override
	public void onClick(View v) {
		if (canChangeFragment) {
			switch (v.getId()) {
			case R.id.footer_myprofile:
				changeFragment(ProfileFragment.class);
				break;
			case R.id.footer_reminder:
				changeFragment(ReminderFragment.class);
				break;
			case R.id.footer_history:
				changeFragment(InvoiceFragment.class);
				break;
			case R.id.footer_terms:
				changeFragment(TermsFragment.class);
				break;
			case R.id.footer_instructions:
				PersistentUtil.setShowHelp(ActivityReminder.this, true);
				Toast.makeText(ActivityReminder.this, getString(R.string.show_help), Toast.LENGTH_SHORT).show();
				break;
			}
		}

	}

	public void changeFragment(Class<?> fragmentClass) {
		hideVisibleFragment();
		if (fragmentClass.equals(ReminderFragment.class)) {
			f = new ReminderFragment();
			toggleTabPressed(TAB_REMINDER);
			getSupportFragmentManager().beginTransaction().disallowAddToBackStack().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).add(R.id.fragment, f, "ReminderFragment").commitAllowingStateLoss();
		} else if (fragmentClass.equals(TermsFragment.class)) {
			f = new TermsFragment();
			toggleTabPressed(TAB_TERMS);
			getSupportFragmentManager().beginTransaction().disallowAddToBackStack().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).add(R.id.fragment, f, "TermsFragment").commitAllowingStateLoss();
		} else if (fragmentClass.equals(ProfileFragment.class)) {
			f = new ProfileFragment();
			toggleTabPressed(TAB_MY_PROFILE);
			getSupportFragmentManager().beginTransaction().disallowAddToBackStack().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).add(R.id.fragment, f, "ProfileFragment").commitAllowingStateLoss();
		} else if (fragmentClass.equals(InvoiceFragment.class)) {
			f = new InvoiceFragment();
			toggleTabPressed(TAB_HISTORY);
			getSupportFragmentManager().beginTransaction().disallowAddToBackStack().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).add(R.id.fragment, f, "InvoiceFragment").commitAllowingStateLoss();
		}
		SettingsTitleBarHandler.initTitleBar(this, getString(R.string.location_settings), true, f.getClass());
	}

	private void hideVisibleFragment() {
		if ((f != null) && f.isAdded()) {
			getSupportFragmentManager().beginTransaction().remove(f).commitAllowingStateLoss();
		}
		getSupportFragmentManager().executePendingTransactions();
	}

	public void saveProfileData() { // will save new profile data
		LogService.log(TAG, "save data");
		if (f.getClass().equals(ProfileFragment.class)) {
			first_name = ((ProfileFragment) f).firstName.getText().toString();
			last_name = ((ProfileFragment) f).lastName.getText().toString();
			email = ((ProfileFragment) f).email.getText().toString();
			changeUserData(true);
		}

	}

	public void showCustomDialog() {// will show dialog to recover your account
		LinearLayout ll_Main = new LinearLayout(this);
		LinearLayout ll_Row1 = new LinearLayout(this);
		LinearLayout ll_Row2 = new LinearLayout(this);
		ll_Main.setOrientation(LinearLayout.VERTICAL);
		ll_Row1.setOrientation(LinearLayout.HORIZONTAL);
		final EditText et_email = new EditText(this);
		TextView tv_email = new TextView(this);
		tv_email.setText(getString(R.string.email_et));
		ll_Row1.addView(tv_email);
		ll_Row1.addView(et_email);
		ll_Main.addView(ll_Row1);
		ll_Main.addView(ll_Row2);
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(getString(R.string.recover_account));
		alert.setView(ll_Main);
		alert.setCancelable(false);
		alert.setPositiveButton(getString(R.string.recover), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String email2 = et_email.getText().toString();
				dialog.dismiss();
				WSCalls.checkExistingUser(email2, loader, ActivityReminder.this, new GetBackFromWS() {

					@Override
					public void success(ArrayList<Taxes> taxes) {
					}

					@Override
					public void success() { // if your profile will exist, it will load it into the profileFragment
						profile = Tools.getProfile(ActivityReminder.this);
						if (f.getClass().equals(ProfileFragment.class)) {
							((ProfileFragment) f).firstName.setText(profile.getFirst_name());
							((ProfileFragment) f).lastName.setText(profile.getLast_name());
							((ProfileFragment) f).email.setText(profile.getEmail());
						}
					}

					@Override
					public void fail(Integer Err) {
					}
				});

			}
		});
		alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = alert.create();
		dialog.show();
	}

	private void changeUserData(boolean isExistingProfile) {
		String valid = Tools.inputIsValid(ActivityReminder.this, first_name, last_name, email);
		if (!isExistingProfile || valid.contains("ready")) {
			loader.setVisibility(View.VISIBLE);
			WSCalls.changeUserData(first_name, last_name, email, isExistingProfile, loader, ActivityReminder.this, new GetBackFromWS() {

				@Override
				public void success(ArrayList<Taxes> taxes) {
				}

				@Override
				public void success() { // if success will save the profile data
					Profile profile = Tools.getProfile(ActivityReminder.this);
					if (f.getClass().equals(ProfileFragment.class)) {
						((ProfileFragment) f).firstName.setText(profile.getFirst_name());
						((ProfileFragment) f).lastName.setText(profile.getLast_name());
						((ProfileFragment) f).email.setText(profile.getEmail());
					}
				}

				@Override
				public void fail(Integer Err) {
				}
			});
		} else {
			Toast.makeText(ActivityReminder.this, valid, Toast.LENGTH_SHORT).show();
		}
	}

	public void toggleTabPressed(Integer tabChosen) {// will select which of the tabs to be selected
		if (tabChosen != 4) {
			myProfile.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.profile, 0, 0);
			history.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.history, 0, 0);
			reminder.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.reminder, 0, 0);
			instructions.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.instructions, 0, 0);
			terms.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.terms, 0, 0);
			switch (tabChosen) {
			case 1:
				myProfile.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.profile_pressed, 0, 0);
				break;
			case 2:
				reminder.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.reminder_pressed, 0, 0);
				break;
			case 3:
				history.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.history_pressed, 0, 0);
				break;
			case 5:
				terms.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.terms_pressed, 0, 0);
				break;
			}
		}
	}

	public void sendInvoice() { // will send invoice via InvoiceFragment
		LogService.log(TAG, "sent invoice");
		if (f.getClass().equals(InvoiceFragment.class)) {
			((InvoiceFragment) f).sendInvoices();
		}
	}

}
