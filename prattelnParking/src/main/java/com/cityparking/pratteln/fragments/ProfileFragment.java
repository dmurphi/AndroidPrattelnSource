package com.cityparking.pratteln.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.cityparking.pratteln.R;
import com.cityparking.pratteln.activities.ActivityReminder;
import com.cityparking.pratteln.entities.Profile;
import com.cityparking.pratteln.utils.LogService;
import com.cityparking.pratteln.utils.Tools;

import java.util.UUID;

public class ProfileFragment extends Fragment {

	public  EditText firstName, lastName, email;
	private Button existingUserButton;
	private Profile profile;

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_my_profile, container, false);
		init(view);
		return view;
	}

	private void init(View view) {
		firstName = (EditText) view.findViewById(R.id.first_name_edit_text);
		lastName = (EditText) view.findViewById(R.id.last_name_edit_text);
		email = (EditText) view.findViewById(R.id.email_edit_text);
		profile = Tools.getProfile(getActivity()); // will get profile from preferences
		existingUserButton = (Button) view.findViewById(R.id.existingUser);
		firstName.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				profile = Tools.getProfile(getActivity());
				if (profile.getFirst_name().contentEquals("firstName") && hasFocus) {
					firstName.setText("");
				}
			}
		});
		lastName.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				profile = Tools.getProfile(getActivity());
				if (profile.getLast_name().contentEquals("lastName") && hasFocus) {
					lastName.setText("");
				}
			}
		});
		email.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				profile = Tools.getProfile(getActivity());
				checkToRemoveFromEmailEditText(profile);
			}
		});
		existingUserButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((ActivityReminder) getActivity()).showCustomDialog();
			}
		});
		if (profile.getFirst_name() != null) {
			firstName.setText(profile.getFirst_name());
			if (profile.getFirst_name().contentEquals("firstName")) {
				firstName.setText("");
			}
		}
		if (profile.getLast_name() != null) {
			lastName.setText(profile.getLast_name());
			if (profile.getLast_name().contentEquals("lastName")) {
				lastName.setText("");
			}
		}
		if (profile.getEmail() != null) {
			checkToRemoveFromEmailEditText(profile);
		}
	}

	private void checkToRemoveFromEmailEditText(final Profile profile) {
		String[] splittedProfile = profile.getEmail().split("@");
		String guIdString = splittedProfile[0];
		LogService.log("", "guId: " + guIdString);
		if (isValid(guIdString)) {
			LogService.log("", "is valid uuid");
		} else {
			LogService.log("", "is NOT valid uuid");
			email.setText(profile.getEmail());
		}
	}

	public boolean isValid(String uuid) {
		if (uuid == null)
			return false;
		try {
			// we have to convert to object and back to string because the built in fromString does not have
			// good validation logic.
			UUID fromStringUUID = UUID.fromString(uuid);
			String toStringUUID = fromStringUUID.toString();
			return toStringUUID.equals(uuid);
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

}
