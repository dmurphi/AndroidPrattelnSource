package com.cityparking.pratteln.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.cityparking.pratteln.R;
import com.cityparking.pratteln.activities.ActivityReminder;
import com.cityparking.pratteln.adapters.InvoiceAdapter;
import com.cityparking.pratteln.entities.Profile;
import com.cityparking.pratteln.entities.UserPayments;
import com.cityparking.pratteln.listeners.GetBackFromInvoices;
import com.cityparking.pratteln.listeners.GetBackWithString;
import com.cityparking.pratteln.utils.DialogFactory;
import com.cityparking.pratteln.utils.LogService;
import com.cityparking.pratteln.utils.Tools;
import com.cityparking.pratteln.webservices.WSCalls;

import java.util.ArrayList;
import java.util.Calendar;

public class InvoiceFragment extends Fragment {

    private ListView invoiceListView;
    private Button fromButton, toButton;
    private ArrayList<UserPayments> invoices = new ArrayList<UserPayments>();
    private InvoiceAdapter invoiceAdapter;
    private long toTime = -1, fromTime = -1;
    private int pageNo = 0;
    public static boolean hasMore = true;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invoice, container, false);
        init(view);
        initAdapter();
        return view;
    }

    private void initAdapter() {
        invoiceAdapter = new InvoiceAdapter(getActivity(), invoices, new GetBackFromInvoices() {

            @Override
            public void success(ArrayList<UserPayments> invoices) { // gets the invoiced back (saved with the checked value)
                InvoiceFragment.this.invoices = invoices;
                for (UserPayments invoice : invoices) {
                    LogService.log("", invoice.toString());
                }
            }

            @Override
            public void success() {
                if (fromTime != -1 && toTime != -1) { // pressed on Load more, it will call getInvoices again, with the next page (pageNo)
                    invoices.remove(invoices.size() - 1);
                    LogService.log("", "pageNo: " + pageNo);
                    ((ActivityReminder) getActivity()).canChangeFragment = false;
                    WSCalls.getInvoices(fromTime, toTime, pageNo, ((ActivityReminder) getActivity()).loader, getActivity(), new GetBackFromInvoices() {

                        @Override
                        public void success(ArrayList<UserPayments> invoices) {
                            // InvoiceFragment.this.invoices.remove(InvoiceFragment.this.invoices.size() - 1);
                            for (UserPayments userPayments : invoices) {
                                userPayments.setIsChecked(false);
                                InvoiceFragment.this.invoices.add(userPayments);
                            }
                            if (hasMore) {
                                UserPayments invoice = new UserPayments(6, getString(R.string.load_more), "", "");
                                InvoiceFragment.this.invoices.add(invoice);
                            }
                            invoiceAdapter.gotNewData(InvoiceFragment.this.invoices);
                            pageNo++;
                            ((ActivityReminder) getActivity()).canChangeFragment = true;

                        }

                        @Override
                        public void success() {
                            // TODO Auto-generated method stub

                        }

                    });
                } else {
                    Toast.makeText(getActivity(), getString(R.string.select_from_time), Toast.LENGTH_SHORT).show();
                }
            }

        });
        invoiceListView.setAdapter(invoiceAdapter);
    }

    private void init(View view) { // will initiate the buttons
        invoiceListView = (ListView) view.findViewById(R.id.invoice_list_view);
        fromButton = (Button) view.findViewById(R.id.fromButton);
        toButton = (Button) view.findViewById(R.id.toButton);
        toTime = System.currentTimeMillis();
        fromTime = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 7;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(toTime); // will set the time for the fromTime and toTime values, used to filter
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        toButton.setText(day + "-" + (month + 1) + "-" + year);
        c.setTimeInMillis(fromTime);
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);
        fromButton.setText(day + "-" + (month + 1) + "-" + year);
        toTime += 1000 * 60 * 60 * 4;
        filterViaDate(fromTime, toTime);
        // date pickers for the FROM and TO buttons
        fromButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                DialogFactory.DatePickerDialog(getActivity(), new GetBackWithString() {

                    @Override
                    public void success(String string) {
                        fromTime = Long.valueOf(string);
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(fromTime);
                        int year = c.get(Calendar.YEAR);
                        int month = c.get(Calendar.MONTH);
                        int day = c.get(Calendar.DAY_OF_MONTH);
                        c.set(year, month, day, 0, 0, 0);
                        fromTime = c.getTimeInMillis();
                        LogService.log("", "From time is: " + fromTime);
                        if (toTime != -1 && fromTime != -1 && fromTime < toTime) {
                            LogService.log("", "will filter the invoices needed");
                            filterViaDate(fromTime, toTime);
                            fromButton.setText(day + "-" + (month + 1) + "-" + year);
                        }
                    }

                });
            }
        });
        toButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                DialogFactory.DatePickerDialog(getActivity(), new GetBackWithString() {

                    @Override
                    public void success(String string) {
                        toTime = Long.valueOf(string);
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(toTime);
                        int year = c.get(Calendar.YEAR);
                        int month = c.get(Calendar.MONTH);
                        int day = c.get(Calendar.DAY_OF_MONTH);
                        c.set(year, month, day, 23, 59, 59);
                        toTime = c.getTimeInMillis();
                        LogService.log("", "From time is: " + toTime);
                        if (toTime != -1 && fromTime != -1 && fromTime < toTime) {
                            LogService.log("", "will filter the invoices needed");
                            filterViaDate(fromTime, toTime);
                            toButton.setText(day + "-" + (month + 1) + "-" + year);
                        }
                    }
                });

            }
        });
    }

    private void filterViaDate(long fromTime, long toTime) { // will call getInvoices.
        LogService.log("", "in filter");
        hasMore = true;
        invoices.clear();
        invoices = new ArrayList<UserPayments>();
        pageNo = 0;
        ((ActivityReminder) getActivity()).canChangeFragment = false; // this value is used so that if a ws is called and response is not back, doesn't allow fragment to change
        WSCalls.getInvoices(fromTime, toTime, pageNo, ((ActivityReminder) getActivity()).loader, getActivity(), new GetBackFromInvoices() {

            @Override
            public void success(ArrayList<UserPayments> invoices) {
                for (UserPayments userPayments : invoices) {
                    userPayments.setIsChecked(false);
                    InvoiceFragment.this.invoices.add(userPayments);
                }
                UserPayments invoice = new UserPayments(6, getString(R.string.load_more), "", "");
                InvoiceFragment.this.invoices.add(invoice);
                invoiceAdapter.gotNewData(InvoiceFragment.this.invoices);
                pageNo++;
                ((ActivityReminder) getActivity()).canChangeFragment = true;

            }

            @Override
            public void success() {
                // TODO Auto-generated method stub

            }
        });
    }

    public void sendInvoices() { // will call WS to send an invoice
        ArrayList<Integer> ids = new ArrayList<Integer>();
        for (UserPayments invoice : invoices) {
            if (invoice.isChecked()) {
                ids.add(invoice.getPaymentId());
            }
        }
        if (ids.size() > 0) {
            Profile profile = Tools.getProfile(getActivity());
            if (profile.getEmail().contains(profile.getUserGuid())) {
                Toast.makeText(getActivity(), getString(R.string.must_create_profile), Toast.LENGTH_SHORT).show();
            } else {
                WSCalls.sendInvoice(ids, ((ActivityReminder) getActivity()).loader, getActivity());
            }
        } else {
            Toast.makeText(getActivity(), getString(R.string.no_invoices), Toast.LENGTH_SHORT).show();
        }
    }
}
