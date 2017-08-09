package com.cityparking.pratteln.adapters;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cityparking.pratteln.R;
import com.cityparking.pratteln.entities.Car;
import com.cityparking.pratteln.listeners.GetBackFromWS;
import com.cityparking.pratteln.receivers.AlarmReceiver;
import com.cityparking.pratteln.utils.PersistentUtil;

import java.util.ArrayList;

public class MyCarsAdapter extends BaseAdapter {

	private ArrayList<Car> carsList = new ArrayList<Car>();

	private Context ctx;

	private GetBackFromWS back;

	public MyCarsAdapter(Context context, ArrayList<Car> cars, GetBackFromWS back) {
		ctx = context;
		this.carsList = cars;
		this.back = back;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		RowHolder holder;
		if (convertView == null) {
			// Inflate new row
			LayoutInflater inflater = ((Activity) ctx).getLayoutInflater();
			convertView = inflater.inflate(R.layout.my_cars_row, parent, false);

			// Instantiate a holder, set references into it and place it in the
			// tag of the convertView
			holder = new RowHolder();
			// Set references to widgets
			holder.delete = (ImageView) convertView.findViewById(R.id.delete);
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.rightPic = (ImageView) convertView.findViewById(R.id.rightpic);
			holder.layout = (RelativeLayout) convertView.findViewById(R.id.layout);
			convertView.setTag(holder);
		} else {
			holder = (RowHolder) convertView.getTag();
		}
		final float begin = (System.currentTimeMillis() - carsList.get(position).getExactTime());
		float time = ((carsList.get(position).getTimeToCount() * 1000) - begin);
		String timeS;
		holder.delete.setVisibility(View.VISIBLE);
		if (time > -1) { // this means the car is parked, and will show how many time it has
			holder.delete.setVisibility(View.GONE);
			int hours = (int) (time / (60 * 60 * 1000));
			int hoursMod = (int) (time % (60 * 60 * 1000));
			int min = (hoursMod / (1000 * 60));
			int minMod = (hoursMod % (1000 * 60));
			int seconds = (minMod / 1000);
			if (hours != 0) {
				timeS = " " + ctx.getString(R.string.parked) + " " + hours + "h " + min + "m " + seconds + "s ";
			} else if (min != 0) {
				timeS = " " + ctx.getString(R.string.parked) + " " + min + "m " + seconds + "s ";
			} else {
				timeS = " " + ctx.getString(R.string.parked) + " " + seconds + "s ";
			}
		} else {
			timeS = "";
			carsList.get(position).setParked(false);
			carsList.get(position).setExactTime(-1);
			carsList.get(position).setTimeToCount(-1);
		}
		holder.name.setText(carsList.get(position).getCnp() + timeS);
		holder.rightPic.setImageResource(R.drawable.accept);
		holder.rightPic.setVisibility(View.INVISIBLE);
		holder.rightPic.setImageResource(R.drawable.accept);
		if (carsList.get(position).getCnp().contentEquals(PersistentUtil.getSelectedCar(ctx))) {
			holder.rightPic.setImageResource(R.drawable.accept);
			holder.rightPic.setVisibility(View.VISIBLE);
		}
		holder.layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) { // clicking on a car will set it as the selected car
				PersistentUtil.setSelectedCar(ctx, carsList.get(position).getCnp());
				back.success();
			}
		});
		holder.delete.setOnClickListener(new OnClickListener() { // if the car is not parked, you can delete it and also cancel its alarm manager

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(ctx, AlarmReceiver.class);
						intent.setAction("intent_myaction_alarm");
						PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, (int) carsList.get(position).getExactTime(), intent, 0);
						AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
						if (alarmManager != null) {
							alarmManager.cancel(pendingIntent);
						}
						if (carsList.get(position).getCnp().contentEquals(PersistentUtil.getSelectedCar(ctx))) {
							PersistentUtil.setSelectedCar(ctx, "-1");
						}
						carsList.remove(position);
						if (carsList.size() < 1) {
							PersistentUtil.setSelectedCar(ctx, "-1");
						}
						notifyDataSetChanged();
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
		return carsList.get(position);
	}

	@Override
	public int getCount() {
		return carsList.size();
	}

	// =========================================================================
	// =========================================================================
	// =========================================================================
	// =========================================================================

	public void gotNewData(ArrayList<Car> cars) {
		this.carsList = new ArrayList<Car>(cars);
		PersistentUtil.setSelectedCar(ctx, cars.get(cars.size() - 1).getCnp());
		notifyDataSetChanged();
	}

	static class RowHolder {
		ImageView delete;
		TextView name;
		ImageView rightPic;
		RelativeLayout layout;
	}

}
