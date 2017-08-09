package com.cityparking.pratteln.webservices;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cityparking.pratteln.R;
import com.cityparking.pratteln.activities.ActivityMain;
import com.cityparking.pratteln.activities.ActivityPayment;
import com.cityparking.pratteln.activities.ActivityRemainingTime;
import com.cityparking.pratteln.activities.ActivityReminder;
import com.cityparking.pratteln.activities.ActivitySplashScreen;
import com.cityparking.pratteln.constants.Constants;
import com.cityparking.pratteln.entities.LastParking;
import com.cityparking.pratteln.entities.Profile;
import com.cityparking.pratteln.entities.Taxes;
import com.cityparking.pratteln.entities.UserPayments;
import com.cityparking.pratteln.fragments.InvoiceFragment;
import com.cityparking.pratteln.listeners.GetBackFromInvoices;
import com.cityparking.pratteln.listeners.GetBackFromWS;
import com.cityparking.pratteln.listeners.GetBackWithString;
import com.cityparking.pratteln.utils.JsonUtil;
import com.cityparking.pratteln.utils.LogService;
import com.cityparking.pratteln.utils.MD5Util;
import com.cityparking.pratteln.utils.PersistentUtil;
import com.cityparking.pratteln.utils.Tools;
import com.cityparking.pratteln.webservices.entity.CallParkingChargesResponseBody;
import com.cityparking.pratteln.webservices.entity.ComputedTariffResponse;
import com.cityparking.pratteln.webservices.entity.FreeParkingResponseBody;
import com.cityparking.pratteln.webservices.entity.InvoiceResponseBody;
import com.cityparking.pratteln.webservices.entity.LocationGlobalDataResponseBody;
import com.cityparking.pratteln.webservices.entity.PaymentTransactionResponse;
import com.cityparking.pratteln.webservices.entity.ProfileResponseBody;
import com.cityparking.pratteln.webservices.entity.ResidentResponseBody;
import com.cityparking.pratteln.webservices.entity.TimeStampResponseBody;
import com.cityparking.pratteln.webservices.entity.VATResponseBody;
import com.cityparking.pratteln.webservices.entity.WSCallsListResponse;
import com.cityparking.pratteln.webservices.entity.WSCallsResponse;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

public class WSCalls {

    private static String TAG = WSCalls.class.getSimpleName();
    static String locID = "4";
    public final static int PAYMENT_FAILED = 10;

    public static void StartParkingCall(final String cnp, final long periodInMinutes, final long newTimeAdded, final int locationAreaId, final int reminder, final RelativeLayout loader, final Context context, final UUID uuid, final String cost, final int maxParkingHour, final boolean extended) {
        final RequestQueue mRequestQueue = Volley.newRequestQueue(context);

        WSCalls.getTimeStamp(mRequestQueue, loader, context, false, new GetBackWithString() {

            @Override
            public void success(String string) {
                LogService.log(TAG, "Starting the webservice call" + string);

                loader.setVisibility(View.VISIBLE);

                String URL;
                if (extended) {
                    URL = Constants.WS_Server + "/Parking/ExtendParking";
                } else {
                    URL = Constants.WS_Server + "/Parking/StartParking";
                }
                // + "&locationId=" + locationId + "&bayId=" + bayId;

                LogService.log(TAG, "URL ::" + URL);
                Profile profile = Tools.getProfile(context);
                JSONObject juser = new JSONObject();
                try {
                    // juser.put("isBethanyBeach", true);
                    juser.put("call_signature", MD5Util.getMd5Hash(Constants.PRESHARED_KEY + "_StartParking_" + string));
                    juser.put("cnp", cnp);
                    juser.put("userEmail", profile.getEmail());
                    juser.put("userGuid", profile.getUserGuid());
                    juser.put("locationId", locID);
                    juser.put("locationAreaId", locationAreaId);
                    juser.put("periodInMinutes", periodInMinutes / 60);
                    juser.put("transactionID", uuid);
                } catch (JSONException e) {

                    e.printStackTrace();
                }
                LogService.log(TAG, "juser ::" + juser);
                final GetBackFromWS errorList = new GetBackFromWS() {

                    @Override
                    public void success(ArrayList<Taxes> taxes) {
                    }

                    @Override
                    public void success() {
                    }

                    @Override
                    public void fail(Integer Err) {
                        if (Err == 0) {
                            StartParkingCall(cnp, periodInMinutes, newTimeAdded, locationAreaId, reminder, loader, context, uuid, cost, maxParkingHour, extended);
                        }
                    }
                };
                LogService.log(TAG, "start juser is: " + juser.toString());
                JsonObjectRequest jr = new JsonObjectRequest(Request.Method.POST, URL, juser, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        LogService.log(TAG, "startparking Response: " + response.toString());

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                loader.setVisibility(View.INVISIBLE);
                                // Set the flag to indicate the timer has been started
                                PersistentUtil.setTimerStarted(context, true);

                                // Start the timer activity and send these values
                                Intent intent = new Intent(context, ActivityRemainingTime.class);
                                // Make all activities on top of ActivityRemainingTime close
                                LogService.log(TAG, "is NOT extended payment");
                                intent.putExtra("timeToCount", periodInMinutes);
                                intent.putExtra("reminder", reminder);
                                intent.putExtra("successMessage", true);
                                intent.putExtra("extended", extended);
                                intent.putExtra("selectedLocationPosition", locationAreaId);
                                if (extended) {
                                    saveLastParkingData(context, cnp, newTimeAdded, cost, maxParkingHour, locationAreaId);
                                } else {
                                    saveLastParkingData(context, cnp, periodInMinutes, cost, maxParkingHour, locationAreaId);
                                }

                                context.startActivity(intent);

                                ((ActivityPayment) context).setResult(Constants.CLOSE_ACTIVITY);
                                ((ActivityPayment) context).finish();
                            }
                        }, 10000);

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        LogService.log(TAG, "Volley error response: " + error.getMessage());
                        loader.setVisibility(View.INVISIBLE);
                        webserviceCallFailedDialog(context.getString(R.string.server_offline_title), context.getString(R.string.server_offline_body), loader, context, errorList);
                    }
                });

                if (Tools.networkIsAvailable((Activity) context)) {
                    mRequestQueue.add(jr);
                } else {
                    webserviceCallFailedDialog(context.getString(R.string.no_internet_connection_title), context.getString(R.string.no_internet_connection_body), loader, context, errorList);
                }
            }
        });
    }

    private static void saveLastParkingData(Context context, String cnp, long periodInMinutes, String cost, int maxParkingHour, int locationAreaId) {
        long currentTimeInMillis = Calendar.getInstance().getTimeInMillis();
        ArrayList<LastParking> parkingEntityList = new ArrayList<LastParking>(PersistentUtil.getPreviousParking(context));
        parkingEntityList.add(new LastParking(cnp, currentTimeInMillis, String.valueOf(periodInMinutes), cost, maxParkingHour, locationAreaId));
        PersistentUtil.savePreviousParking(context, parkingEntityList);
    }

    public static void checkExistingUser(final String email, final RelativeLayout loader, final Context context, final GetBackFromWS listener) {
        final RequestQueue mRequestQueue = Volley.newRequestQueue(context);
        WSCalls.getTimeStamp(mRequestQueue, loader, context, false, new GetBackWithString() {

            @Override
            public void success(String string) {
                LogService.log(TAG, "Starting the webservice call for uuid");

                String URL = Constants.WS_Server + "/Account/ExistingUser";
                JSONObject juser = new JSONObject();
                try {
                    juser.put("call_signature", MD5Util.getMd5Hash(Constants.PRESHARED_KEY + "_ExistingUser_" + string));
                    LogService.log(TAG, "call_sig: " + MD5Util.getMd5Hash(Constants.PRESHARED_KEY + "_ExistingUser_" + string));
                    juser.put("email", email);
                } catch (JSONException e) {

                    e.printStackTrace();
                }
                final GetBackFromWS errorList = new GetBackFromWS() {

                    @Override
                    public void success(ArrayList<Taxes> taxes) {

                    }

                    @Override
                    public void success() {

                    }

                    @Override
                    public void fail(Integer Err) {
                        if (Err == 0) {
                            checkExistingUser(email, loader, context, listener);
                        }
                    }
                };
                LogService.log(TAG, "ZZZZ URL: " + URL);
                LogService.log(TAG, "ZZZZ juser: " + juser);
                JsonObjectRequest jr = new JsonObjectRequest(Request.Method.POST, URL, juser, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        WSCallsResponse<ProfileResponseBody> profileResponse;
                        LogService.log(TAG, "Response: " + response.toString());
                        try {
                            profileResponse = JsonUtil.jsonToObject(response.toString(), new TypeToken<WSCallsResponse<ProfileResponseBody>>() {
                            }.getType());
                            if (profileResponse.getErrorMessage().equals("")) {
                                final String firstName = profileResponse.getResponseBody().getFirstName();
                                final String lastName = profileResponse.getResponseBody().getLastName();
                                final String email = profileResponse.getResponseBody().getEmail();
                                WSCalls.changeUserData(firstName, lastName, email, false, loader, context, new GetBackFromWS() {

                                    @Override
                                    public void success(ArrayList<Taxes> taxes) {
                                    }

                                    @Override
                                    public void success() {
                                        Profile profile = new Profile(firstName, lastName, email, Tools.getProfile(context).getUserGuid());
                                        Tools.setProfile(profile, context);
                                        listener.success();
                                    }

                                    @Override
                                    public void fail(Integer Err) {
                                    }
                                });
                                loader.setVisibility(View.INVISIBLE);
                            } else {
                                Toast.makeText(context, profileResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();
                                loader.setVisibility(View.INVISIBLE);
                            }
                        } catch (Exception e) {
                            LogService.log(TAG, "Existing user Exception: " + e.getMessage());
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        LogService.log(TAG, "Volley error response: " + error.getMessage());
                        loader.setVisibility(View.INVISIBLE);
                        webserviceCallFailedDialog(context.getString(R.string.server_offline_title), context.getString(R.string.server_offline_body), loader, context, errorList);
                    }
                });

                if (Tools.networkIsAvailable((Activity) context)) {
                    mRequestQueue.add(jr);
                } else {
                    webserviceCallFailedDialog(context.getString(R.string.no_internet_connection_title), context.getString(R.string.no_internet_connection_body), loader, context, errorList);
                }
            }
        });

    }

    public static void changeUserData(final String newFirstName, final String newLastName, final String newEmail, final boolean existing, final RelativeLayout loader, final Context context, final GetBackFromWS listener) {
        final RequestQueue mRequestQueue = Volley.newRequestQueue(context);
        WSCalls.getTimeStamp(mRequestQueue, loader, context, false, new GetBackWithString() {

            @Override
            public void success(String string) {
                LogService.log(TAG, "Starting the webservice call");

                loader.setVisibility(View.VISIBLE);

                String URL = Constants.WS_Server + "/Account/ChangeUser";
                // String URL = "http://host30.reea.net:1012/Locations/ParkingSpots?call_signature=1";
                final Profile profile = Tools.getProfile(context);
                JSONObject juser = new JSONObject();
                try {
                    juser.put("call_signature", MD5Util.getMd5Hash(Constants.PRESHARED_KEY + "_ChangeUser_" + string));
                    LogService.log(TAG, "call_sig: " + MD5Util.getMd5Hash(Constants.PRESHARED_KEY + "_ChangeUser_" + string));
                    juser.put("firstName", profile.getFirst_name());
                    juser.put("lastName", profile.getLast_name());
                    juser.put("email", profile.getEmail());
                    juser.put("newFirstName", newFirstName);
                    juser.put("newLastName", newLastName);
                    juser.put("newEmail", newEmail);
                    juser.put("newUser", existing);
                    juser.put("userGuid", profile.getUserGuid());
                } catch (JSONException e) {

                    e.printStackTrace();
                }
                final GetBackFromWS errorList = new GetBackFromWS() {

                    @Override
                    public void success(ArrayList<Taxes> taxes) {

                    }

                    @Override
                    public void success() {

                    }

                    @Override
                    public void fail(Integer Err) {
                        if (Err == 0) {
                            changeUserData(newFirstName, newLastName, newEmail, existing, loader, context, listener);
                        }
                    }
                };
                LogService.log(TAG, "URL ::" + URL);
                LogService.log(TAG, "juser ::" + juser.toString());

                //
                // JsonObjectRequest jr = new JsonObjectRequest(Request.Method.POST, URL, null, new Response.Listener<JSONObject>() {
                JsonObjectRequest jr = new JsonObjectRequest(Request.Method.POST, URL, juser, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        LogService.log(TAG, "extend Response: " + response.toString());
                        loader.setVisibility(View.INVISIBLE);
                        try {
                            Boolean ready = response.getBoolean("succeeded");
                            if (ready) {
                                profile.setEmail(newEmail);
                                profile.setFirst_name(newFirstName);
                                profile.setLast_name(newLastName);
                                profile.setUserGuid(Tools.getProfile(context).getUserGuid());
                                Tools.setProfile(profile, context);
                                Toast.makeText(context, context.getString(R.string.saved_profile), Toast.LENGTH_SHORT).show();
                                loader.setVisibility(View.INVISIBLE);
                                listener.success();
                            } else {
                                webserviceCallFailedDialog(context.getString(R.string.wrong_email), context.getString(R.string.please_check), loader, context, errorList);
                            }
                        } catch (JSONException e) {
                            webserviceCallFailedDialog(context.getString(R.string.server_offline_title), context.getString(R.string.server_offline_body), loader, context, errorList);
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        LogService.log(TAG, "Volley error response: " + error.getMessage());
                        loader.setVisibility(View.INVISIBLE);
                        if (existing) {
                            webserviceCallFailedDialog(context.getString(R.string.wrong_email), context.getString(R.string.please_check), loader, context, errorList);
                        } else {
                            webserviceCallFailedDialog(context.getString(R.string.server_offline_title), context.getString(R.string.server_offline_body), loader, context, errorList);
                        }
                    }
                });

                if (Tools.networkIsAvailable((Activity) context)) {
                    mRequestQueue.add(jr);
                } else {
                    webserviceCallFailedDialog(context.getString(R.string.no_internet_connection_title), context.getString(R.string.no_internet_connection_body), loader, context, errorList);
                }
            }
        });

    }

    public static void sendInvoice(final ArrayList<Integer> ids, final RelativeLayout loader, final Context context) {
        final RequestQueue mRequestQueue = Volley.newRequestQueue(context);
        WSCalls.getTimeStamp(mRequestQueue, loader, context, false, new GetBackWithString() {

            @Override
            public void success(String string) {
                LogService.log(TAG, "Starting the webservice call");

                loader.setVisibility(View.VISIBLE);

                String URL = Constants.WS_Server + "/Invoice/Invoices";
                // String URL = "http://host30.reea.net:1012/Locations/ParkingSpots?call_signature=1";
                final Profile profile = Tools.getProfile(context);
                JSONObject juser = new JSONObject();
                try {
                    juser.put("call_signature", MD5Util.getMd5Hash(Constants.PRESHARED_KEY + "_Invoices_" + string));
                    LogService.log(TAG, "call_sig: " + MD5Util.getMd5Hash(Constants.PRESHARED_KEY + "_Invoices_" + string));
                    juser.put("email", profile.getEmail());
                    juser.put("paymentIds", ids);
                    juser.put("locationId", locID);
                } catch (JSONException e) {

                    e.printStackTrace();
                }
                LogService.log(TAG, "URL ::" + URL);
                LogService.log(TAG, "juser ::" + juser.toString());
                final GetBackFromWS errorList = new GetBackFromWS() {

                    @Override
                    public void success(ArrayList<Taxes> taxes) {

                    }

                    @Override
                    public void success() {

                    }

                    @Override
                    public void fail(Integer Err) {
                        if (Err == 0) {
                            sendInvoice(ids, loader, context);
                        }
                    }
                };

                //
                // JsonObjectRequest jr = new JsonObjectRequest(Request.Method.POST, URL, null, new Response.Listener<JSONObject>() {
                JsonObjectRequest jr = new JsonObjectRequest(Request.Method.POST, URL, juser, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        LogService.log(TAG, "extend Response: " + response.toString());
                        loader.setVisibility(View.INVISIBLE);
                        try {
                            Boolean ready = response.getBoolean("succeeded");
                            if (ready) {
                                Toast.makeText(context, context.getString(R.string.invoice_request), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, context.getString(R.string.invoice_error), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {

                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        LogService.log(TAG, "Volley error response: " + error.getMessage());
                        loader.setVisibility(View.INVISIBLE);
                        webserviceCallFailedDialog(context.getString(R.string.server_offline_title), context.getString(R.string.server_offline_body), loader, context, errorList);
                    }
                });

                if (Tools.networkIsAvailable((Activity) context)) {
                    mRequestQueue.add(jr);
                } else {
                    webserviceCallFailedDialog(context.getString(R.string.no_internet_connection_title), context.getString(R.string.no_internet_connection_body), loader, context, errorList);
                }
            }
        });

    }

    public static void getInvoices(final long startTime, final long endTime, final int pageNo, final RelativeLayout loader, final Context context, final GetBackFromInvoices listener) {
        final RequestQueue mRequestQueue = Volley.newRequestQueue(context);
        WSCalls.getTimeStamp(mRequestQueue, loader, context, true, new GetBackWithString() {

            @Override
            public void success(String string) {
                LogService.log(TAG, "Starting the webservice call");

                loader.setVisibility(View.VISIBLE);

                String URL = Constants.WS_Server + "/Invoice/Payments";
                // String URL = "http://host30.reea.net:1012/Locations/ParkingSpots?call_signature=1";
                final Profile profile = Tools.getProfile(context);
                JSONObject juser = new JSONObject();
                try {
                    juser.put("call_signature", MD5Util.getMd5Hash(Constants.PRESHARED_KEY + "_Payments_" + string));
                    LogService.log(TAG, "call_sig: " + MD5Util.getMd5Hash(Constants.PRESHARED_KEY + "_Payments_" + string));
                    juser.put("email", profile.getEmail());
                    juser.put("startTime", startTime);
                    juser.put("endTime", endTime);
                    juser.put("pageNo", pageNo);
                    juser.put("locationId", locID);
                } catch (JSONException e) {

                    e.printStackTrace();
                }
                LogService.log(TAG, "URL ::" + URL);
                LogService.log(TAG, "juser ::" + juser.toString());
                final GetBackFromWS errorList = new GetBackFromWS() {

                    @Override
                    public void success(ArrayList<Taxes> taxes) {

                    }

                    @Override
                    public void success() {

                    }

                    @Override
                    public void fail(Integer Err) {
                        if (Err == 0) {
                            getInvoices(startTime, endTime, pageNo, loader, context, listener);
                        }
                    }
                };

                //
                // JsonObjectRequest jr = new JsonObjectRequest(Request.Method.POST, URL, null, new Response.Listener<JSONObject>() {
                JsonObjectRequest jr = new JsonObjectRequest(Request.Method.POST, URL, juser, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        WSCallsResponse<InvoiceResponseBody> invoiceBody = new WSCallsResponse<InvoiceResponseBody>();
                        LogService.log(TAG, "extend Response: " + response.toString());
                        try {
                            invoiceBody = JsonUtil.jsonToObject(response.toString(), new TypeToken<WSCallsResponse<InvoiceResponseBody>>() {
                            }.getType());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        ArrayList<UserPayments> invoices = invoiceBody.getResponseBody().getUserPayments();
                        InvoiceFragment.hasMore = invoiceBody.getResponseBody().getHasMore();
                        loader.setVisibility(View.INVISIBLE);
                        listener.success(invoices);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ((ActivityReminder) context).canChangeFragment = true;
                        LogService.log(TAG, "Volley error response: " + error.getMessage());
                        loader.setVisibility(View.INVISIBLE);
                        webserviceCallFailedDialog(context.getString(R.string.server_offline_title), context.getString(R.string.server_offline_body), loader, context, errorList);
                    }
                });

                if (Tools.networkIsAvailable((Activity) context)) {
                    mRequestQueue.add(jr);
                } else {
                    ((ActivityReminder) context).canChangeFragment = true;
                    webserviceCallFailedDialog(context.getString(R.string.no_internet_connection_title), context.getString(R.string.no_internet_connection_body), loader, context, errorList);
                }
            }
        });

    }

    public static void SendUUID(final RequestQueue mRequestQueue, final UUID uuid, final RelativeLayout loader, final Context context, final GetBackFromWS listener) {

        WSCalls.getTimeStamp(mRequestQueue, loader, context, false, new GetBackWithString() {

            @Override
            public void success(String string) {
                LogService.log(TAG, "Starting the webservice call for uuid");

                String URL = Constants.WS_Server + "/Payment/PaymentResult?transactionID=" + uuid + "&call_signature=" + MD5Util.getMd5Hash(Constants.PRESHARED_KEY + "_PaymentResult_" + string);
                System.out.println("-----------------URL PAYMENT: " + URL);
                final GetBackFromWS errorList = new GetBackFromWS() {

                    @Override
                    public void success(ArrayList<Taxes> taxes) {

                    }

                    @Override
                    public void success() {

                    }

                    @Override
                    public void fail(Integer Err) {
                        if (Err == 0) {
                            SendUUID(mRequestQueue, uuid, loader, context, listener);
                        }
                    }
                };
                JsonObjectRequest jr = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        WSCallsResponse<PaymentTransactionResponse> sendUUID;
                        LogService.log(TAG, "Response: " + response.toString());
                        try {
                            sendUUID = JsonUtil.jsonToObject(response.toString(), new TypeToken<WSCallsResponse<PaymentTransactionResponse>>() {
                            }.getType());
                            Boolean ready = sendUUID.getResponseBody().getPaymentTransactionResponse().getTransactionReady();
                            String errorMessage = sendUUID.getErrorMessage();
                            if (errorMessage.equals("")) {
                                if (ready) {
                                    String refCardNr = (String) sendUUID.getResponseBody().getPaymentTransactionResponse().getRefCardNo();
                                    PersistentUtil.setCardNr(context, refCardNr);
                                    listener.success();
                                } else {
                                    System.err.println("--- sendUUID.getErrorMessage() " + sendUUID.getErrorMessage());
                                    if (!TextUtils.isEmpty(sendUUID.getErrorMessage())) {
                                        // go back to parking screen and show error msg
                                        listener.fail(PAYMENT_FAILED);
                                    } else {
                                        final Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (Constants.inPayment) {
                                                    mRequestQueue.cancelAll(new RequestQueue.RequestFilter() {
                                                        @Override
                                                        public boolean apply(Request<?> request) {
                                                            return true;
                                                        }
                                                    });
                                                    SendUUID(mRequestQueue, uuid, loader, context, listener);
                                                }
                                            }
                                        }, 2000);
                                    }
                                }
                            } else {
                                webserviceCallFailedDialog(context.getString(R.string.server_offline_title), errorMessage, loader, context, errorList);
                            }
                        } catch (Exception e) {
                            LogService.log(TAG, "UUID Exception: " + e.getMessage());
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        LogService.log(TAG, "Volley error response: " + error.getMessage());
                        loader.setVisibility(View.INVISIBLE);
                        webserviceCallFailedDialog(context.getString(R.string.server_offline_title), context.getString(R.string.server_offline_body), loader, context, errorList);
                    }
                });

                if (Tools.networkIsAvailable((Activity) context)) {
                    mRequestQueue.add(jr);
                } else {
                    webserviceCallFailedDialog(context.getString(R.string.no_internet_connection_title), context.getString(R.string.no_internet_connection_body), loader, context, errorList);
                }
            }
        });

    }

    public static void createInitialUser(final RelativeLayout loader, final Context context, final GetBackFromWS listener) {
        final RequestQueue mRequestQueue = Volley.newRequestQueue(context);
        WSCalls.getTimeStamp(mRequestQueue, loader, context, false, new GetBackWithString() {

            @Override
            public void success(String string) {
                LogService.log(TAG, "Starting the webservice call for uuid");

                String URL = Constants.WS_Server + "/Account/InitialUser?call_signature=" + MD5Util.getMd5Hash(Constants.PRESHARED_KEY + "_InitialUser_" + string);
                LogService.log(TAG, "ZZZZ URL: " + URL);
                final GetBackFromWS errorList = new GetBackFromWS() {

                    @Override
                    public void success(ArrayList<Taxes> taxes) {

                    }

                    @Override
                    public void success() {

                    }

                    @Override
                    public void fail(Integer Err) {
                        if (Err == 0) {
                            createInitialUser(loader, context, listener);
                        }
                    }
                };
                JsonObjectRequest jr = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        WSCallsResponse<ProfileResponseBody> profileResponse;
                        LogService.log(TAG, "Response: " + response.toString());
                        try {

                            profileResponse = JsonUtil.jsonToObject(response.toString(), new TypeToken<WSCallsResponse<ProfileResponseBody>>() {
                            }.getType());
                            String firstName = profileResponse.getResponseBody().getFirstName();
                            String email = profileResponse.getResponseBody().getEmail();
                            String lastName = profileResponse.getResponseBody().getLastName();
                            String userGuid = profileResponse.getResponseBody().getUserGuid();
                            Profile profile = new Profile(firstName, lastName, email, userGuid);
                            Tools.setProfile(profile, context);
                            listener.success();
                        } catch (Exception e) {
                            Toast.makeText(context, context.getString(R.string.parse_error_body), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        LogService.log(TAG, "Volley error response: " + error.getMessage());
                        loader.setVisibility(View.INVISIBLE);
                        webserviceCallFailedDialog(context.getString(R.string.server_offline_title), context.getString(R.string.server_offline_body), loader, context, errorList);
                    }
                });
                Crashlytics.setString("hasInternetConnection: ", "" + Tools.networkIsAvailable(context));
                Crashlytics.logException(new Exception("Is network: " + Tools.networkIsAvailable(context)));
                if (Tools.networkIsAvailable((Activity) context)) {
                    mRequestQueue.add(jr);
                } else {
                    webserviceCallFailedDialog(context.getString(R.string.no_internet_connection_title), context.getString(R.string.no_internet_connection_body), loader, context, errorList);
                }
            }
        });

    }

    public static void getVat(final String vat, final RelativeLayout loader, final Context context, final GetBackFromWS listener) {
        final RequestQueue mRequestQueue = Volley.newRequestQueue(context);
        WSCalls.getTimeStamp(mRequestQueue, loader, context, false, new GetBackWithString() {

            @Override
            public void success(String string) {
                LogService.log(TAG, "Starting the webservice call for uuid");

                loader.setVisibility(View.VISIBLE);

                String URL = Constants.WS_Server + "/Payment/VATAmount?countryCode=" + vat + "&call_signature=" + MD5Util.getMd5Hash(Constants.PRESHARED_KEY + "_VATAmount_" + string);
                final GetBackFromWS errorList = new GetBackFromWS() {

                    @Override
                    public void success(ArrayList<Taxes> taxes) {

                    }

                    @Override
                    public void success() {

                    }

                    @Override
                    public void fail(Integer Err) {
                        if (Err == 0) {
                            getVat(vat, loader, context, listener);
                        }
                    }
                };
                JsonObjectRequest jr = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        WSCallsResponse<VATResponseBody> vatResponse;
                        LogService.log(TAG, "Response: " + response.toString());
                        try {
                            vatResponse = JsonUtil.jsonToObject(response.toString(), new TypeToken<WSCallsResponse<VATResponseBody>>() {
                            }.getType());
                            Constants.VAT = vatResponse.getResponseBody().getVAT();
                            listener.success();
                            loader.setVisibility(View.INVISIBLE);
                        } catch (Exception e) {
                            LogService.log(TAG, "Json Exception occurred: " + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        LogService.log(TAG, "Volley error response: " + error.getMessage());
                        loader.setVisibility(View.INVISIBLE);
                        webserviceCallFailedDialog(context.getString(R.string.server_offline_title), context.getString(R.string.server_offline_body), loader, context, errorList);
                    }
                });

                if (Tools.networkIsAvailable((Activity) context)) {
                    mRequestQueue.add(jr);
                } else {
                    webserviceCallFailedDialog(context.getString(R.string.no_internet_connection_title), context.getString(R.string.no_internet_connection_body), loader, context, errorList);
                }
            }
        });

    }

    private static void webserviceCallFailedDialog(String dialogHeader, String dialogBody, final RelativeLayout loader, final Context context, final GetBackFromWS listener) {
        LogService.log("", "Failed Dialog");
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(dialogHeader);
        builder.setMessage(dialogBody);

        // Capture the back button press so we can hide the loader
        builder.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                    loader.setVisibility(View.INVISIBLE);
                    if (listener != null) {
                        listener.fail(1);
                    }
                }
                return true;
            }
        });

        builder.setPositiveButton(context.getString(R.string.retry), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // StartParkingCall(call_signature, spotId, periodInMinutes,
                // locationId, bayId);
                LogService.log("", "Retry pressed");
                if (listener != null) {
                    listener.fail(0);
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                LogService.log("", "Cancel pressed");
                loader.setVisibility(View.INVISIBLE);
                ((Activity) context).finish();
                if (listener != null) {
                    listener.fail(1);
                }
            }
        });

        if (Looper.myLooper() == Looper.getMainLooper()) {
            try {
                builder.show();
            } catch (Exception e) {
                LogService.err(TAG, e.getMessage(), e);
            }
        }
    }

    public static void getTimeStamp(final RequestQueue mRequestQueue, final RelativeLayout loader, final Context context, final boolean fromGetInvoices, final GetBackWithString listener) {

        loader.setVisibility(View.VISIBLE);
        String URL = Constants.WS_Server + "/Account/Timestamp";
        LogService.log(TAG, "URL ::" + URL);
        final GetBackFromWS errorList = new GetBackFromWS() {

            @Override
            public void success(ArrayList<Taxes> taxes) {
            }

            @Override
            public void success() {
            }

            @Override
            public void fail(Integer Err) {
                if (Err == 0) {
                    getTimeStamp(mRequestQueue, loader, context, fromGetInvoices, listener);
                }
            }
        };
        JsonObjectRequest jr = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                TimeStampResponseBody timeStamp;

                LogService.log(TAG, "Response: " + response.toString());
                try {
                    timeStamp = JsonUtil.jsonToObject(response.toString(), TimeStampResponseBody.class);
                    listener.success(timeStamp.getTimestamp());
                } catch (Exception e) {
                    Log.e(TAG, "Exception occurred: " + e);
                } catch (OutOfMemoryError e) {
                    Log.e(TAG, "Exception occurred: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (fromGetInvoices) {
                    ((ActivityReminder) context).canChangeFragment = true;
                }
                LogService.err(TAG, "Volley error response: " + error.getMessage(), error);
                loader.setVisibility(View.INVISIBLE);
                webserviceCallFailedDialog(context.getString(R.string.server_offline_title), context.getString(R.string.server_offline_body), loader, context, errorList);
            }
        });

        if (Tools.networkIsAvailable((Activity) context)) {
            mRequestQueue.add(jr);
        } else {
            if (fromGetInvoices) {
                ((ActivityReminder) context).canChangeFragment = true;
            }
            webserviceCallFailedDialog(context.getString(R.string.no_internet_connection_title), context.getString(R.string.no_internet_connection_body), loader, context, errorList);
            new RuntimeException(errorList.toString());
        }
    }

    public static void callFreeParkingAvailable(final RelativeLayout loader, final Context context, final String cardNumber, final int locationAreaId, final boolean forFreeParking, final GetBackFromWS listener) {
        final RequestQueue mRequestQueue = Volley.newRequestQueue(context);
        WSCalls.getTimeStamp(mRequestQueue, loader, context, false, new GetBackWithString() {

            @Override
            public void success(String string) {

                loader.setVisibility(View.VISIBLE);
                String cnp = cardNumber.replace(" ", "%20");

                Profile profile = Tools.getProfile(context);
                String URL = Constants.WS_Server + "/LocationData/FreeParking?call_signature=02&locationCityId=" + locID + "&userId=" + profile.getUserGuid() + "&cnp=" + cnp + "&locationAreaId=" + locationAreaId + "&isFreeParking=" + forFreeParking;
                LogService.log(TAG, "=========Free parking url: " + URL);
                final GetBackFromWS errorList = new GetBackFromWS() {

                    @Override
                    public void success(ArrayList<Taxes> taxes) {
                    }

                    @Override
                    public void success() {
                    }

                    @Override
                    public void fail(Integer Err) {
                        if (Err == 0) {
                            callFreeParkingAvailable(loader, context, cardNumber, locationAreaId, forFreeParking, listener);
                        }

                    }
                };

                JsonObjectRequest jr = new JsonObjectRequest(URL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        WSCallsResponse<FreeParkingResponseBody> freeParking;
                        try {
                            if (response.getBoolean("succeeded")) {
                                freeParking = JsonUtil.jsonToObject(response.toString(), new TypeToken<WSCallsResponse<FreeParkingResponseBody>>() {
                                }.getType());
                                PersistentUtil.setFreeParking(context, freeParking.getResponseBody().getFreeParkingDuration());
                                PersistentUtil.setDirectiveId(context, freeParking.getResponseBody().getFreeParkingDirectiveId());
                            } else {
                                PersistentUtil.setFreeParking(context, 0);
                                PersistentUtil.setDirectiveId(context, 0);
                            }
                            listener.success();
                        } catch (Exception e) {
                            LogService.err(TAG, "Some error occurred: " + e.getMessage(), e);
                            loader.setVisibility(View.INVISIBLE);
                            webserviceCallFailedDialog(context.getString(R.string.parse_error_title), context.getString(R.string.parse_error_body), loader, context, errorList);
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError arg0) {
                    }
                });

                if (Tools.networkIsAvailable((ActivityMain) context)) {
                    mRequestQueue.add(jr);
                } else {
                    webserviceCallFailedDialog(context.getString(R.string.no_internet_connection_title), context.getString(R.string.no_internet_connection_body), loader, context, errorList);
                }
            }
        });
    }

    public static void callCancelFreeParking(final RelativeLayout loader, final Context context, final GetBackFromWS listener) {
        final RequestQueue mRequestQueue = Volley.newRequestQueue(context);
        WSCalls.getTimeStamp(mRequestQueue, loader, context, false, new GetBackWithString() {

            @Override
            public void success(String string) {
                loader.setVisibility(View.VISIBLE);
                String URL = Constants.WS_Server + "/LocationData/CancelFreeParking?call_signature=02&directiveId=" + PersistentUtil.getDirectiveId(context);
                final GetBackFromWS errorList = new GetBackFromWS() {

                    @Override
                    public void success(ArrayList<Taxes> taxes) {
                    }

                    @Override
                    public void success() {
                    }

                    @Override
                    public void fail(Integer Err) {
                        if (Err == 0) {
                            callCancelFreeParking(loader, context, listener);
                        }

                    }
                };

                JsonObjectRequest jr = new JsonObjectRequest(URL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getBoolean("succeeded")) {
                                listener.success();
                            }
                        } catch (Exception e) {
                            LogService.err(TAG, "Some error occurred: " + e.getMessage(), e);
                            loader.setVisibility(View.INVISIBLE);
                            webserviceCallFailedDialog(context.getString(R.string.parse_error_title), context.getString(R.string.parse_error_body), loader, context, errorList);
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError arg0) {
                    }
                });

                if (Tools.networkIsAvailable((ActivityPayment) context)) {
                    mRequestQueue.add(jr);
                } else {
                    webserviceCallFailedDialog(context.getString(R.string.no_internet_connection_title), context.getString(R.string.no_internet_connection_body), loader, context, errorList);
                }
            }
        });
    }

    public static void callLocationsGlobalData(final RelativeLayout loader, final Context context, final GetBackFromWS listener) {
        final RequestQueue mRequestQueue = Volley.newRequestQueue(context);

        WSCalls.getTimeStamp(mRequestQueue, loader, context, false, new GetBackWithString() {

            @Override
            public void success(String string) {

                loader.setVisibility(View.VISIBLE);

                String URL = Constants.WS_Server + "/LocationData/LocationsGlobalData?call_signature=02&locationId=" + locID;

                final GetBackFromWS errorList = new GetBackFromWS() {

                    @Override
                    public void success(ArrayList<Taxes> taxes) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void success() {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void fail(Integer Err) {
                        if (Err == 0) {
                            callLocationsGlobalData(loader, context, listener);
                        }
                    }
                };

                JsonObjectRequest jr = new JsonObjectRequest(URL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        WSCallsResponse<LocationGlobalDataResponseBody> locations;
                        try {
                            locations = JsonUtil.jsonToObject(response.toString(), new TypeToken<WSCallsResponse<LocationGlobalDataResponseBody>>() {
                            }.getType());
                            PersistentUtil.setParkingLocation(locations.getResponseBody());
                            listener.success();
                            loader.setVisibility(View.INVISIBLE);
                        } catch (Exception e) {
                            LogService.err(TAG, "Some error occurred: " + e.getMessage(), e);
                            loader.setVisibility(View.INVISIBLE);
                            webserviceCallFailedDialog(context.getString(R.string.parse_error_title), context.getString(R.string.parse_error_body), loader, context, errorList);
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
                Crashlytics.setString("hasInternetConnection: ", "" + Tools.networkIsAvailable(context));
                Crashlytics.logException(new Exception("Is network: " + Tools.networkIsAvailable(context)));
                if (Tools.networkIsAvailable(context)) {
                    mRequestQueue.add(jr);
                } else {
                    webserviceCallFailedDialog(context.getString(R.string.no_internet_connection_title), context.getString(R.string.no_internet_connection_body), loader, context, errorList);
                }
            }
        });
    }

    // ws call for special users (they will have other parking prices and more locations)
    public static void callSpecialResident(final RelativeLayout loader, final Context context, final String cardNumber, final int locationAreaId, final GetBackFromWS listener) {

        loader.setVisibility(View.VISIBLE);
        String cnp = cardNumber.replace(" ", "%20");
        RequestQueue mRequestQueue = Volley.newRequestQueue(context);
        String URL = Constants.WS_Server + "/LocationData/Resident?call_signature=02&cnp=" + cnp + "&locationAreaId=" + locationAreaId;
        final GetBackFromWS errorList = new GetBackFromWS() {

            @Override
            public void success(ArrayList<Taxes> taxes) {
            }

            @Override
            public void success() {
            }

            @Override
            public void fail(Integer Err) {
                if (Err == 0) {
                    callSpecialResident(loader, context, cardNumber, locationAreaId, listener);
                }

            }
        };

        LogService.log(TAG, "==========Special Resident Call");
        JsonObjectRequest jr = new JsonObjectRequest(URL, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                WSCallsResponse<ResidentResponseBody> residentSpecial;
                try {
                    residentSpecial = JsonUtil.jsonToObject(response.toString(), new TypeToken<WSCallsResponse<ResidentResponseBody>>() {
                    }.getType());
                    PersistentUtil.setResident(context, residentSpecial.getResponseBody().isResident());
                    PersistentUtil.setResidentLocation(residentSpecial.getResponseBody().getResidentTariff());
                    loader.setVisibility(View.INVISIBLE);
                    listener.success();
                } catch (Exception e) {
                    LogService.err(TAG, "Some error occurred: " + e.getMessage(), e);
                    loader.setVisibility(View.INVISIBLE);
                    webserviceCallFailedDialog(context.getString(R.string.parse_error_title), context.getString(R.string.parse_error_body), loader, context, errorList);
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError arg0) {
            }
        });


        if (Tools.networkIsAvailable((ActivityMain) context)) {
            mRequestQueue.add(jr);
        } else {
            webserviceCallFailedDialog(context.getString(R.string.no_internet_connection_title), context.getString(R.string.no_internet_connection_body), loader, context, errorList);
        }
    }

    // ws call for Computed Tariff
    public static void callComputedTariff(final RelativeLayout loader, final Context context, final String carNumber, final int locationAreaId, final int selectedParkingDuration, final GetBackFromWS listener) {

        loader.setVisibility(View.VISIBLE);
        String cnp;
        if (carNumber.equalsIgnoreCase("-1")) {
            cnp = "";
        } else {
            cnp = carNumber.replace(" ", "%20");
        }

        String previousParkingDuration = "0";
        double previousParkingAmountPaid = 0;

        ArrayList<LastParking> previousParkings = PersistentUtil.getPreviousParking(context);
        if (previousParkings != null && previousParkings.size() > 0) {
            for (LastParking p : previousParkings) {
                if (p.getCnp().equalsIgnoreCase(cnp)) {
                    previousParkingAmountPaid = previousParkingAmountPaid + Double.valueOf(p.getLastParkingCost());
                    previousParkingDuration = p.getLastParkingDuration();
                }
            }

        }

        RequestQueue mRequestQueue = Volley.newRequestQueue(context);
        String URL = Constants.WS_Server + "/Payment/ComputedTariff?call_signature=02&locationId=" + locID + "&locationAreaId=" + locationAreaId + "&cnp=" + cnp + "&selectedParkingDuration=" + selectedParkingDuration + "&previousParkingDuration=" + previousParkingDuration +
                "&previousParkingAmountPaid=" + previousParkingAmountPaid;
        Log.d(TAG, "----- COMPUTED URL: " + URL);
        final GetBackFromWS errorList = new GetBackFromWS() {

            @Override
            public void success(ArrayList<Taxes> taxes) {
            }

            @Override
            public void success() {
            }

            @Override
            public void fail(Integer Err) {
                if (Err == 0) {
                    callComputedTariff(loader, context, carNumber, locationAreaId, selectedParkingDuration, listener);
                }

            }
        };

        JsonObjectRequest jr = new JsonObjectRequest(URL, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                WSCallsResponse<ComputedTariffResponse> computedTariff;
                try {
                    computedTariff = JsonUtil.jsonToObject(response.toString(), new TypeToken<WSCallsResponse<ComputedTariffResponse>>() {
                    }.getType());
                    if (computedTariff.getResponseBody().getTariff() != null) {
                        PersistentUtil.setString(context, computedTariff.getResponseBody().getTariff().getComputedValue(), PersistentUtil.COMPUTED_TARIFF);
                        PersistentUtil.setBoolean(context, computedTariff.getResponseBody().getTariff().isHasSpecialTimeLimit(), PersistentUtil.HAS_SPECIAL_TIME_LIMIT);
                        PersistentUtil.setString(context, computedTariff.getResponseBody().getTariff().getTimeLimit(), PersistentUtil.SPECIAL_TIME_LIMIT);
                        PersistentUtil.setString(context, computedTariff.getResponseBody().getTariff().getParkingDurationMinutes(), PersistentUtil.PARKING_DURATION_MINUTES);
                        PersistentUtil.setString(context, computedTariff.getResponseBody().getTariff().getFreeParkingDurationMinutes(), PersistentUtil.FREE_PARKING_DURATION_MINUTES);
                        PersistentUtil.setBoolean(context, computedTariff.getResponseBody().getTariff().isWhiteListCNP(), PersistentUtil.WHITE_LIST_CNP);
                        listener.success();
                    } else {
                        Toast.makeText(context, "There went something wrong whe calculating tariff", Toast.LENGTH_SHORT).show();
                    }
                    loader.setVisibility(View.INVISIBLE);
                } catch (Exception e) {
                    LogService.err(TAG, e.getMessage(), e);
                    loader.setVisibility(View.INVISIBLE);
                    webserviceCallFailedDialog(context.getString(R.string.parse_error_title), context.getString(R.string.parse_error_body), loader, context, errorList);
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError arg0) {
                LogService.log(TAG, "VOLLEYERROR: " + arg0.getMessage());
            }
        });

        if (Tools.networkIsAvailable(context)) {
            mRequestQueue.add(jr);
        } else {
            webserviceCallFailedDialog(context.getString(R.string.no_internet_connection_title), context.getString(R.string.no_internet_connection_body), loader, context, errorList);
        }
    }


    /**
     * Makes a call to the webservice and retrieves the required information regarding the parking charges
     */
    public static void webserviceCallParkingCharges(final RelativeLayout loader, final Context context, final GetBackFromWS listener) {
        WSCalls.getTimeStamp(loader, context, false, new GetBackWithString() {

            @Override
            public void success(String string) {
                LogService.log(TAG, "Starting the parking charges webservice call");

                loader.setVisibility(View.VISIBLE);

                RequestQueue mRequestQueue = Volley.newRequestQueue(context);

                String URL = Constants.WS_Server + "/Parking/ParkingCharges?call_signature=" + MD5Util.getMd5Hash(Constants.PRESHARED_KEY + "_ParkingCharges_" + string) + "&locationId=" + locID;
                LogService.log(TAG, "URL ::" + Constants.PRESHARED_KEY + "_ParkingCharges_" + string);
                LogService.log(TAG, "URL ::" + URL);
                final GetBackFromWS errorList = new GetBackFromWS() {

                    @Override
                    public void success(ArrayList<Taxes> taxes) {

                    }

                    @Override
                    public void success() {

                    }

                    @Override
                    public void fail(Integer Err) {
                        if (Err == 0) {
                            webserviceCallParkingCharges(loader, context, listener);
                        }
                    }
                };
                JsonObjectRequest jr = new JsonObjectRequest(URL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        WSCallsListResponse<CallParkingChargesResponseBody> parkingCharges = new WSCallsListResponse<CallParkingChargesResponseBody>();
                        LogService.log(TAG, "Response: " + response.toString());

                        try {
                            parkingCharges = JsonUtil.jsonToObject(response.toString(), new TypeToken<WSCallsListResponse<CallParkingChargesResponseBody>>() {
                            }.getType());
                            LogService.log(TAG, "WOHOO : Got answer from webservice: " + parkingCharges.getResponseBody().getClass());
                            ArrayList<Taxes> taxes = new ArrayList<Taxes>();
                            for (CallParkingChargesResponseBody rb : parkingCharges.getResponseBody()) {
                                // Get the values minutes-rates from the object
                                Double mins = rb.getMinutes();
                                Double rate = rb.getRate();
                                Taxes tax = new Taxes(rate, mins);
                                taxes.add(tax);
                                LogService.log(TAG, mins + " ------------ " + rate);
                            }

                            listener.success(taxes);

                        } catch (Exception e) {
                            LogService.err(TAG, "Couldn't parse json!", e);
                            loader.setVisibility(View.INVISIBLE);
                            webserviceCallFailedDialog(context.getString(R.string.parse_error_title), context.getString(R.string.parse_error_body), loader, context, errorList);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        LogService.err(TAG, "Volley error response: " + error.getMessage(), error);
                        loader.setVisibility(View.INVISIBLE);
                        webserviceCallFailedDialog(context.getString(R.string.server_offline_title), context.getString(R.string.server_offline_body), loader, context, errorList);
                    }
                });

                if (Tools.networkIsAvailable((Activity) context)) {
                    mRequestQueue.add(jr);
                } else {
                    webserviceCallFailedDialog(context.getString(R.string.no_internet_connection_title), context.getString(R.string.no_internet_connection_body), loader, context, errorList);
                }
            }
        });

    }

    private static void getTimeStamp(final RelativeLayout loader, final Context context, final boolean fromGetInvoices, final GetBackWithString listener) {
        loader.setVisibility(View.VISIBLE);
        RequestQueue mRequestQueue = Volley.newRequestQueue(context);
        String URL = Constants.WS_Server + "/Account/Timestamp";
        LogService.log(TAG, "URL ::" + URL);
        final GetBackFromWS errorList = new GetBackFromWS() {

            @Override
            public void success(ArrayList<Taxes> taxes) {
            }

            @Override
            public void success() {
            }

            @Override
            public void fail(Integer Err) {
                if (Err == 0) {
                    getTimeStamp(loader, context, fromGetInvoices, listener);
                }
            }
        };
        JsonObjectRequest jr = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                TimeStampResponseBody timeStamp = new TimeStampResponseBody();

                LogService.log(TAG, "Response: " + response.toString());
                try {
                    timeStamp = JsonUtil.jsonToObject(response.toString(), TimeStampResponseBody.class);
                    listener.success(timeStamp.getTimestamp());
                } catch (Exception e) {
                    Log.e(TAG, "Exception occured: " + e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (fromGetInvoices) {
                    ((ActivityReminder) context).canChangeFragment = true;
                }
                LogService.err(TAG, "Volley error response: " + error.getMessage(), error);
                loader.setVisibility(View.INVISIBLE);
                webserviceCallFailedDialog(context.getString(R.string.server_offline_title), context.getString(R.string.server_offline_body), loader, context, errorList);
            }
        });

        if (Tools.networkIsAvailable((Activity) context)) {
            mRequestQueue.add(jr);
        } else {
            if (fromGetInvoices) {
                ((ActivityReminder) context).canChangeFragment = true;
            }
            webserviceCallFailedDialog(context.getString(R.string.no_internet_connection_title), context.getString(R.string.no_internet_connection_body), loader, context, errorList);
        }
    }

}
