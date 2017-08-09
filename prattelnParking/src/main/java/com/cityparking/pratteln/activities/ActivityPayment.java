package com.cityparking.pratteln.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.cityparking.pratteln.R;
import com.cityparking.pratteln.constants.Constants;
import com.cityparking.pratteln.entities.Taxes;
import com.cityparking.pratteln.listeners.GetBackFromWS;
import com.cityparking.pratteln.titlebar.TitleBarHandler;
import com.cityparking.pratteln.utils.DialogFactory;
import com.cityparking.pratteln.utils.LogService;
import com.cityparking.pratteln.utils.PersistentUtil;
import com.cityparking.pratteln.utils.Tools;
import com.cityparking.pratteln.webservices.WSCalls;

import java.util.ArrayList;
import java.util.UUID;

public class ActivityPayment extends Activity {
    private static final String TAG = ActivityPayment.class.getSimpleName();

    private WebView webView;

    private UUID tmpUuid;

    private String cardNo = "";
    private String url;
    private String cost;

    private int selectedPosition = 0;
    private long timeToCount, timeRemained;
    private long newTimeAdded = 0;
    private int reminder;

    private boolean extended;
    private int maxParkingHours = 0;

    @Override
    protected void onDestroy() {
        LogService.log(TAG, "onDestroy payment activity");
        if (PersistentUtil.getFreeParking(ActivityPayment.this) > 0) {
            cancelFreeParking();
        }
        Constants.inPayment = false;
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        Constants.inPayment = true;

        PersistentUtil.setInt(this, -1, "selected_location_pos");
        PersistentUtil.setInt(this, -1, "selected_parking_period");
        // Initialize the title bar
        TitleBarHandler.initTitleBar(this, getResources().getString(R.string.action_payment), true, false);
        final UUID uuid = Tools.generateGuid();
        tmpUuid = uuid;
        LogService.log(TAG, "GUUID: " + uuid + "in payment: " + Constants.inPayment);
        getIntentExtra();

		/*
         * Set up webview
		 */
        // Get reference to webview
        final String reqType;
        if (extended) {
            reqType = "Payment";
        } else {
            reqType = "Payment";
        }
        cardNo = PersistentUtil.getCardNr(this);
        // WS to get VAT back from the server
        WSCalls.getVat("US", (RelativeLayout) findViewById(R.id.splash_screen_loader_container), ActivityPayment.this, new GetBackFromWS() {

            private int pagefinishedCounter;

            @Override
            public void success(ArrayList<Taxes> taxes) {
                // TODO Auto-generated method stub
            }

            @SuppressLint("SetJavaScriptEnabled")
            @Override
            public void success() {
                // loads the webview for pmt
                pagefinishedCounter = 0;
                // initiates the url that will be loaded in the webview
                url = Constants.paymentUrl + "?RequestType=" + reqType + "&MachineID=" + "HECPratteln" + "&CustomerNo=" + "6953" + "&ReservationID=" + "856985" + "&LocationID=4" + "&Amount=" + cost + "&Currency=" + Constants.CHF + "&Language=" + getString(R.string.lang) + "&plateID=" + PersistentUtil.getSelectedCar(ActivityPayment.this) + "&StartTime=" + System.currentTimeMillis() + "&EndTime="
                        + (System.currentTimeMillis() + Constants.timeToCount * 1000) + "&TransactionID=" + uuid + "&VAT=" + Constants.VAT + "&RefCardNo=" + cardNo + "&RefFileNo=" + "";
                String fingerprint = Tools.md5(cost + "CHF6953");
                url += "&Fingerprint=" + fingerprint; // ads the finger print (md5 encryption) to the url
                LogService.log(TAG, "================URL PAYMENT: " + url);
                webView = (WebView) findViewById(R.id.payment_webview);
                LogService.log(TAG, "URL to pmt: " + url);
                WebSettings webSettings = webView.getSettings();
                webSettings.setJavaScriptEnabled(true);
                webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
                webSettings.setSaveFormData(false);
                webView.loadUrl(url);
                // Set the listeners for page loads
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url2) {
                        LogService.log(TAG, "Page Finished : " + url2);
                        // will start sending uuid, if it enters success will call start/extend parking, then if is success, it will start timer
                        super.onPageFinished(view, url2);
                        if (pagefinishedCounter == 0) {
                            pagefinishedCounter++;
                            final Handler handler = new Handler();
                            long timetoDelay = 30000;
                            if (extended) {
                                timetoDelay = 10000;
                            }
                            final RequestQueue mRequestQueue = Volley.newRequestQueue(ActivityPayment.this);
                            LogService.log(TAG, "time to delay: " + timetoDelay);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    WSCalls.SendUUID(mRequestQueue, uuid, (RelativeLayout) findViewById(R.id.splash_screen_loader_container), ActivityPayment.this, new GetBackFromWS() {

                                        @Override
                                        public void success(ArrayList<Taxes> taxes) {
                                        }

                                        @Override
                                        public void success() {
                                            LogService.log(TAG, "Success sending uuid, will call startparking");
                                            callWSForParking(); // checks to see wether to extend the parking or create a new parking
                                        }

                                        @Override
                                        public void fail(Integer Err) {
                                            DialogFactory.showErrorDialog(ActivityPayment.this, "Error", "Payment failed");

                                        }
                                    });
                                }
                            }, timetoDelay);
                        } else {
                            LogService.log(TAG, "URL is different" + url2);
                        }

                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        LogService.log(TAG, "Page loading : " + url);
                        return super.shouldOverrideUrlLoading(view, url);
                    }
                });
            }

            @Override
            public void fail(Integer Err) {
                // TODO Auto-generated method stub
                DialogFactory.showErrorDialog(ActivityPayment.this, "Error", "Payment failed");
            }
        });

    }

    private void getIntentExtra() {
         /*
         * Get the selected values by the user
		 */

        Intent intent = getIntent();
        if (intent != null) {
            extended = intent.getBooleanExtra("extended", false);
            maxParkingHours = intent.getIntExtra("maxParkingHours", 0);
            if (extended) {
                timeToCount = Tools.getSeconds(0, intent.getLongExtra("timeAdded", 0));
                newTimeAdded = Tools.getSeconds(0, intent.getLongExtra("newTime", 0));
                cost = intent.getStringExtra("cost");
            } else {
                timeToCount = intent.getLongExtra("timeToCount", 0);
                reminder = intent.getIntExtra("reminder", -1);
                cost = intent.getStringExtra("cost");
            }
            selectedPosition = intent.getIntExtra("selectedLocationPosition", 0);
            LogService.log(TAG, "time added is in payment: " + timeToCount);
            LogService.log(TAG, "bundle not null Extended: " + extended);
        } else {
            LogService.errline(TAG, "bundle was null. Can not continue with payment process");
        }


    }

    private void callWSForParking() {
        WSCalls.StartParkingCall(PersistentUtil.getSelectedCar(ActivityPayment.this), timeToCount, newTimeAdded, selectedPosition, reminder, (RelativeLayout) findViewById(R.id.splash_screen_loader_container), ActivityPayment.this, tmpUuid, cost, maxParkingHours, extended);
    }

    private void cancelFreeParking() {
        WSCalls.callCancelFreeParking((RelativeLayout) findViewById(R.id.splash_screen_loader_container), ActivityPayment.this, new GetBackFromWS() {

            @Override
            public void success(ArrayList<Taxes> taxes) {
                // TODO Auto-generated method stub
            }

            @Override
            public void success() {
            }

            @Override
            public void fail(Integer Err) {
                // TODO Auto-generated method stub
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
