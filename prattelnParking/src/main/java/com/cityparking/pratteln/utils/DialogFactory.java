package com.cityparking.pratteln.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;

import com.cityparking.pratteln.R;
import com.cityparking.pratteln.listeners.GetBackWithString;

import java.util.Calendar;

public class DialogFactory {

    protected static long time;
    protected static AlertDialog testAlertDialog;

    public static void showInfoDialog(final Context context, String title, String message) {

        AlertDialog.Builder infoDialogBuilder = new AlertDialog.Builder(context);

        // set title
        infoDialogBuilder.setTitle(title);

        // set message
        infoDialogBuilder.setMessage(message);

        // set click listener
        infoDialogBuilder.setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                testAlertDialog = null;
            }
        });

        if (testAlertDialog == null) {
            // create alert dialog
            testAlertDialog = infoDialogBuilder.create();
            // show it
            testAlertDialog.show();
        }
    }

    public static void showErrorDialog(final Context context, String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        // set title
        alertDialogBuilder.setTitle(title);

        // set dialog message
        alertDialogBuilder.setMessage(message).setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // if this button is clicked, close
                // current activity
                // MainActivity.this.finish();
            }
        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public static void DatePickerDialog(final Context context, final GetBackWithString back) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.datepicker_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(true);
        Button positiveB = (Button) dialog.findViewById(R.id.positive_button);
        Button negativeB = (Button) dialog.findViewById(R.id.negative_button);
        DatePicker datepicker = (DatePicker) dialog.findViewById(R.id.datePicker1);
        setCurrentDateOnView(datepicker);
        positiveB.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                back.success(time + "");
                dialog.dismiss();
            }
        });
        negativeB.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public static void setCurrentDateOnView(DatePicker datepicker) {

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        time = c.getTimeInMillis();
        // set current date into date picker
        datepicker.init(year, month, day, new OnDateChangedListener() {

            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar calTemp = Calendar.getInstance();
                calTemp.set(year, monthOfYear, dayOfMonth);
                LogService.log("", calTemp.toString());
                time = calTemp.getTimeInMillis();
            }
        });
    }
}
