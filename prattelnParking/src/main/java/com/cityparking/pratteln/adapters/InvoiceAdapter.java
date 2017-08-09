package com.cityparking.pratteln.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cityparking.pratteln.R;
import com.cityparking.pratteln.entities.UserPayments;
import com.cityparking.pratteln.fragments.InvoiceFragment;
import com.cityparking.pratteln.listeners.GetBackFromInvoices;
import com.cityparking.pratteln.utils.PersistentUtil;
import com.cityparking.pratteln.utils.Tools;

import java.text.NumberFormat;
import java.util.ArrayList;

public class InvoiceAdapter extends BaseAdapter {

    private ArrayList<UserPayments> invoices = new ArrayList<UserPayments>();

    private Context ctx;

    private GetBackFromInvoices back;

    public InvoiceAdapter(Context context, ArrayList<UserPayments> invoices, GetBackFromInvoices back) {
        ctx = context;
        this.invoices = invoices;
        this.back = back;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        RowHolder holder;
        if (convertView == null) {
            // Inflate new row
            LayoutInflater inflater = ((Activity) ctx).getLayoutInflater();
            convertView = inflater.inflate(R.layout.invoices_row, parent, false);

            // Instantiate a holder, set references into it and place it in the
            // tag of the convertView
            holder = new RowHolder();
            // Set references to widgets
            holder.date = (TextView) convertView.findViewById(R.id.date);
            holder.cnp = (TextView) convertView.findViewById(R.id.car_number_plate);
            holder.sum = (TextView) convertView.findViewById(R.id.sum);
            holder.rightPic = (ImageView) convertView.findViewById(R.id.rightpic);
            holder.layout = (RelativeLayout) convertView.findViewById(R.id.layout);
            convertView.setTag(holder);
        } else {
            holder = (RowHolder) convertView.getTag();
        }

        holder.layout.setBackgroundColor(invoices.get(position).getIsFreeParking() ? ctx.getResources().getColor(R.color.free_parking_bg_green) : ctx.getResources().getColor(R.color.transparent));

        if (!invoices.get(position).isChecked()) {
            holder.rightPic.setVisibility(View.INVISIBLE);
        } else {
            holder.rightPic.setVisibility(View.VISIBLE);
        }

        final RowHolder fHolder = holder;

        if (!invoices.get(position).getCarNumberPlate().contentEquals(ctx.getString(R.string.load_more))) {
            holder.layout.setVisibility(View.VISIBLE);
            holder.cnp.setText(invoices.get(position).getCarNumberPlate() + " | " + invoices.get(position).getLocationAreaId());
        } else if (invoices.get(position).getCarNumberPlate().contentEquals(ctx.getString(R.string.load_more)) && InvoiceFragment.hasMore) {
            holder.layout.setVisibility(View.VISIBLE);
            holder.cnp.setText(invoices.get(position).getCarNumberPlate());
        } else {
            holder.layout.setVisibility(View.GONE);
        }

        if (!invoices.get(position).getStartTime().trim().contentEquals("") && !invoices.get(position).getStartTime().trim().contentEquals(ctx.getResources().getString(R.string.load_more_info))) {
            holder.date.setText(Tools.getDate(Long.valueOf(invoices.get(position).getStartTime())));
        } else {
            holder.date.setText("");
        }

        if (!invoices.get(position).getAmount().trim().contentEquals("") && !invoices.get(position).getAmount().trim().contentEquals(ctx.getResources().getString(R.string.load_more_info))) {
            // this IF contains the logic of the list items
            NumberFormat format = NumberFormat.getNumberInstance();
            format.setMinimumFractionDigits(2);
            format.setMaximumFractionDigits(2);
            String output = format.format(((Float.valueOf(invoices.get(position).getAmount()) / 100f)));
            String currency = PersistentUtil.getCurrency(ctx);
            if (currency.equalsIgnoreCase("USD")) {
                holder.sum.setText("$ " + output);
            } else {
                holder.sum.setText(output + " " + currency);
            }
        } else if (invoices.get(position).getCarNumberPlate().contentEquals(ctx.getString(R.string.load_more))) {
            holder.sum.setText("");
        } else {
            holder.sum.setText(ctx.getResources().getString(R.string.invoices_free_parking));
        }

        if (invoices.get(position).isChecked()) {
            holder.rightPic.setVisibility(View.VISIBLE);
        } else {
            holder.rightPic.setVisibility(View.INVISIBLE);
        }

        holder.layout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (invoices.get(position).getCarNumberPlate().contentEquals(ctx.getString(R.string.load_more)) && InvoiceFragment.hasMore) {
                    back.success();
                } else {
                    if (!invoices.get(position).isChecked()) {
                        invoices.get(position).setIsChecked(true);
                        fHolder.rightPic.setVisibility(View.VISIBLE);
                    } else {
                        invoices.get(position).setIsChecked(false);
                        fHolder.rightPic.setVisibility(View.INVISIBLE);
                    }
                    back.success(invoices);
                }
            }
        });
        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return invoices.get(position);
    }

    @Override
    public int getCount() {
        return invoices.size();
    }

    // =========================================================================
    // =========================================================================
    // =========================================================================
    // =========================================================================

    public void gotNewData(ArrayList<UserPayments> invoices) {
        this.invoices = new ArrayList<UserPayments>(invoices);
        notifyDataSetChanged();
    }

    static class RowHolder {
        TextView cnp;
        TextView sum;
        TextView date;
        ImageView rightPic;
        RelativeLayout layout;
    }

}
