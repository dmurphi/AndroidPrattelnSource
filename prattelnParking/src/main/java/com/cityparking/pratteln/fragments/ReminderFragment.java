package com.cityparking.pratteln.fragments;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;

import com.cityparking.pratteln.R;
import com.cityparking.pratteln.utils.PersistentUtil;
import com.cityparking.pratteln.utils.Tools;

@SuppressLint("NewApi")
public class ReminderFragment extends Fragment implements OnClickListener {

    private Button beed10_btn, beed15_btn, beed20_btn, beed25_btn, beed30_btn;
    private Switch switchReminder;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reminder, container, false);
        init(view);
        return view;
    }

    private void init(View view) {
        switchReminder = (Switch) view.findViewById(R.id.reminder_on_off_switch);
        switchReminder.setChecked(PersistentUtil.getBoolean(getActivity(), PersistentUtil.REMINDER_ON));

        switchReminder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PersistentUtil.setBoolean(getActivity(), isChecked, PersistentUtil.REMINDER_ON);
            }
        });

        // Get beeds references
        beed10_btn = (Button) view.findViewById(R.id.beed_10);
        beed15_btn = (Button) view.findViewById(R.id.beed_15);
        beed20_btn = (Button) view.findViewById(R.id.beed_20);
        beed25_btn = (Button) view.findViewById(R.id.beed_25);
        beed30_btn = (Button) view.findViewById(R.id.beed_30);

        // Set event listeners for the beeds
        beed10_btn.setOnClickListener(this);
        beed15_btn.setOnClickListener(this);
        beed20_btn.setOnClickListener(this);
        beed25_btn.setOnClickListener(this);
        beed30_btn.setOnClickListener(this);

        drawUI(view);

        int selectedBeed = PersistentUtil.getSelectedBeed(getActivity());
        if (selectedBeed != -1) {
            resetBeeds();
            switch (selectedBeed) {
                case 1:
                    toggleBeedSelection(beed10_btn);
                    break;
                case 2:
                    toggleBeedSelection(beed15_btn);
                    break;
                case 3:
                    toggleBeedSelection(beed20_btn);
                    break;
                case 4:
                    toggleBeedSelection(beed25_btn);
                    break;
                case 5:
                    toggleBeedSelection(beed30_btn);
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.beed_10:
            case R.id.beed_15:
            case R.id.beed_20:
            case R.id.beed_25:
            case R.id.beed_30:
                toggleBeedSelection((Button) v);
                break;
        }
    }

    /**
     * Performs actions on UI views (resizing, text placement, drawable placement, etc)
     */
    private void drawUI(View view) {

		/*
         * Set the dimensions and padding for beeds container
		 */
        // Get reference
        LinearLayout beedsContainer = (LinearLayout) view.findViewById(R.id.reminder_beeds_container);
        // Get the dimensions of the screen
        int[] screenDim = Tools.getScreenDimensions(getActivity());
        // Get layout params
        RelativeLayout.LayoutParams beedsContainerParams = (RelativeLayout.LayoutParams) beedsContainer.getLayoutParams();
        // Set the dimensions of the layout
        int beedsW = (int) (screenDim[0] * 0.95f); // 95% the width of the
        // screen
        int beedsH = (beedsW / 9); // This makes the views inside the
        // layout have square shapes
        beedsContainerParams.width = beedsW;
        beedsContainerParams.height = beedsH;
        beedsContainer.setLayoutParams(beedsContainerParams);

		/*
		 * Set the text for the reminder info. Can not be done in xml because of white highlighted word in text
		 */
        String reminderText = "<html><body style=\"padding:0; margin:0;\"><font color=\"";
        reminderText += "#343432";
        reminderText += "\"><p align=\"justify\">";
        reminderText += getString(R.string.reminder_info_begin);
        reminderText += " ";
        reminderText += getString(R.string.reminder_info_end);
        reminderText += "</p></font></body></html>";

        WebView reminderWebView = (WebView) view.findViewById(R.id.reminder_info);
        reminderWebView.setBackgroundColor(0x00000000);

        WebSettings webSettings = reminderWebView.getSettings();
        webSettings.setDefaultTextEncodingName("utf-8");
        // webSettings.setDefaultFixedFontSize(getResources().getDimension(R.dimen.reminder_info_size));
        reminderWebView.loadDataWithBaseURL(null, reminderText, "text/html", "UTF-8", null);

    }

    /**
     * Determines if the button needs to be selected or deselected and performs actions in that regard. Changes background drawable, text color, and sets appropriate variables.
     *
     * @param beed
     */
    @SuppressWarnings("JavaDoc")
    private void toggleBeedSelection(Button beed) {

        // Get resource reference
        Resources res = getResources();

        // Get resource index of colors
        int beedSelectedTextColor = res.getColor(R.color.beed_selected_text);
        int beedDefaultTextColor = res.getColor(R.color.beed_not_selected_text);

        // Determine if beed is currently selected or not depending on the current text color
        boolean isSelected = (beed.getCurrentTextColor() == beedSelectedTextColor);

        // Reset all the beeds just in case there's one already selected
        resetBeeds();

        // Set the background, depending on the type of button this is (middle or at extremeties)
        int beedIndex = -1;
        switch (beed.getId()) {
            case R.id.beed_10:
                beedIndex = 1;
                beed.setBackgroundResource(isSelected ? R.drawable.shape_circle_left : R.drawable.shape_circle_left_selected);
                break;
            case R.id.beed_15:
                beedIndex = 2;
                beed.setBackgroundResource(isSelected ? R.drawable.shape_circle : R.drawable.shape_circle_selected);
                break;
            case R.id.beed_20:
                beedIndex = 3;
                beed.setBackgroundResource(isSelected ? R.drawable.shape_circle : R.drawable.shape_circle_selected);
                break;
            case R.id.beed_25:
                beedIndex = 4;
                beed.setBackgroundResource(isSelected ? R.drawable.shape_circle : R.drawable.shape_circle_selected);
                break;
            case R.id.beed_30:
                beedIndex = 5;
                beed.setBackgroundResource(isSelected ? R.drawable.shape_circle_right : R.drawable.shape_circle_right_selected);
                break;
        }

        // Set text color
        if (isSelected) {
            beed.setTextColor(beedDefaultTextColor);
        } else {
            beed.setTextColor(beedSelectedTextColor);
        }

        // Set variables which keep track of the state. If the beed was previously selected (isSelected) then we're deselecting it and we need to reset to -1
        PersistentUtil.setSelectedBeed(getActivity(), (isSelected ? -1 : beedIndex));
    }

    /**
     * Sets all beeds to not-selected state
     */
    private void resetBeeds() {

        // Reset drawables for all beeds
        beed10_btn.setBackgroundResource(R.drawable.shape_circle_left);
        beed15_btn.setBackgroundResource(R.drawable.shape_circle);
        beed20_btn.setBackgroundResource(R.drawable.shape_circle);
        beed25_btn.setBackgroundResource(R.drawable.shape_circle);
        beed30_btn.setBackgroundResource(R.drawable.shape_circle_right);
        // Reset text colors for all beeds
        beed10_btn.setTextColor(getResources().getColor(R.color.beed_not_selected_text));
        beed15_btn.setTextColor(getResources().getColor(R.color.beed_not_selected_text));
        beed20_btn.setTextColor(getResources().getColor(R.color.beed_not_selected_text));
        beed25_btn.setTextColor(getResources().getColor(R.color.beed_not_selected_text));
        beed30_btn.setTextColor(getResources().getColor(R.color.beed_not_selected_text));
        // Reset variables which keeps track of the states
        PersistentUtil.setSelectedBeed(getActivity(), -1);

    }

}
