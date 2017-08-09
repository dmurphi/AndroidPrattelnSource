package com.cityparking.pratteln.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cityparking.pratteln.R;
import com.cityparking.pratteln.adapters.MyCarsAdapter;
import com.cityparking.pratteln.entities.Car;
import com.cityparking.pratteln.entities.Cars;
import com.cityparking.pratteln.entities.Taxes;
import com.cityparking.pratteln.listeners.GetBackFromWS;
import com.cityparking.pratteln.utils.LogService;
import com.cityparking.pratteln.utils.PersistentUtil;
import com.cityparking.pratteln.utils.Tools;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityMyCarsList extends Activity implements OnClickListener { // this activity lets you select which car to use

	public static String TAG = ActivityMyCarsList.class.getSimpleName();

	private TextView backButton, editButton, myCarsTextView, selectFromCarsTV;
	private ListView myCars;
	private MyCarsAdapter myCarsAdapter;
	private Boolean gotoTimer = false;
	private Cars cars = new Cars();
	private Timer myTimer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_my_cars);
		init();

	}

	private void init() {
		backButton = (TextView) findViewById(R.id.back_my_cars);
		editButton = (TextView) findViewById(R.id.edit);
		selectFromCarsTV = (TextView) findViewById(R.id.select_from_cars_list);
		myCarsTextView = (TextView) findViewById(R.id.my_cars);
		myCars = (ListView) findViewById(R.id.cars_list_view);
		Bundle b = this.getIntent().getExtras();
		if (b != null) {
			gotoTimer = b.getBoolean("gototimer");
		}
		// if it goes to timer, then it does not need to show the edit button

		LogService.log(TAG, "gototimer: " + gotoTimer);
		backButton.setOnClickListener(this);
		editButton.setOnClickListener(this);
		if (gotoTimer) { // edit button is set to Gone and also sets correct strings
			myCarsTextView.setText(getString(R.string.my_parked_cars));
			selectFromCarsTV.setText(getString(R.string.select_a_car));
			editButton.setVisibility(View.GONE);
		}

		GetBackFromWS back = new GetBackFromWS() { // sets the listener for the myCarsAdapter

			@Override
			public void success(ArrayList<Taxes> taxes) {
			}

			@Override
			public void success() {
				if (gotoTimer) {
					startActivity(new Intent(ActivityMyCarsList.this, ActivityRemainingTime.class));
					finish();
				} else {
					Car carTemp = null;
					myCarsAdapter.notifyDataSetChanged();
					for (Car car : cars) { // checks and sets the selected car.
						if (car.getCnp().contentEquals(PersistentUtil.getSelectedCar(ActivityMyCarsList.this))) {
							carTemp = car;
						}
					}
					if (carTemp != null) { // if the car is parked will go to ActivityRemainingTime if not to ActivityMain
						final float begin = (System.currentTimeMillis() - carTemp.getExactTime());
						float time = ((carTemp.getTimeToCount() * 1000) - begin);
						Tools.setCars(cars, ActivityMyCarsList.this);
						if (carTemp.getExactTime() != -1 && carTemp.getTimeToCount() != -1 && time > -1) { // is parked
							Intent intent = new Intent(ActivityMyCarsList.this, ActivityRemainingTime.class);
							startActivity(intent);
							finish();
						} else {
							Intent intent = new Intent(ActivityMyCarsList.this, ActivityMain.class);
							startActivity(intent);
							finish();
						}
					}

				}
			}

			@Override
			public void fail(Integer Err) {
				// TODO Auto-generated method stub

			}
		};
		cars = Tools.getCars(ActivityMyCarsList.this); // gets cars from preferences
		if (!gotoTimer && cars.size() < 1) { // if no car selected it will show a popup to add new car
			selectFromCarsTV.setText(getString(R.string.add_first_car_notif));
			showCustomDialog();
		}
		if (gotoTimer) { // if goToTimer means that at least 1 car is parked, and sets the logic for the cases
			editButton.setVisibility(View.GONE);
			Cars cars2 = new Cars();
			// checks to only show the cars that are parked
			for (Car car : cars) {
				final float begin = (System.currentTimeMillis() - car.getExactTime());
				float time = ((car.getTimeToCount() * 1000) - begin);
				if (car.getExactTime() != -1 && car.getTimeToCount() != -1 && time > -1) {
					cars2.add(car);
				}
			}
			// selects where to go next
			if (cars2.size() < 1) {
				PersistentUtil.setMainPageForBack(ActivityMyCarsList.this, PersistentUtil.ACTIVITY_MAIN);
				startActivity(new Intent(ActivityMyCarsList.this, ActivityMain.class));
				finish();
			} else if (cars2.size() != 1) {
				PersistentUtil.setMainPageForBack(ActivityMyCarsList.this, PersistentUtil.ACTIVITY_MY_CARS_LIST);
				myCarsAdapter = new MyCarsAdapter(ActivityMyCarsList.this, cars2, back);
			} else {
				PersistentUtil.setMainPageForBack(ActivityMyCarsList.this, PersistentUtil.ACTIVITY_REMAINING_TIME);
				PersistentUtil.setSelectedCar(ActivityMyCarsList.this, cars2.get(0).getCnp());
				startActivity(new Intent(ActivityMyCarsList.this, ActivityRemainingTime.class));
				finish();
			}
		} else {
			myCarsAdapter = new MyCarsAdapter(ActivityMyCarsList.this, cars, back);
		}
		myCars.setAdapter(myCarsAdapter);
		TimerTask myTask = new TimerTask() { // timer task will refresh the list from 1 to 1 minute

			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						if (myCarsAdapter != null) {
							myCarsAdapter.notifyDataSetChanged();
						}
					}
				});
				LogService.log(TAG, "in timer task");
			}
		};
		myTimer = new Timer();
		myTimer.schedule(myTask, 60 * 1000, 60 * 1000);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_my_cars:
			if (gotoTimer) {
				finish();
			} else {
				// if a car is selected, it will exit
				if (!PersistentUtil.getSelectedCar(ActivityMyCarsList.this).contentEquals("-1")) {
					Intent intent = new Intent(ActivityMyCarsList.this, ActivityMain.class);
					startActivity(intent);
					finish();
					Tools.setCars(cars, ActivityMyCarsList.this);
				} else {
					Toast.makeText(ActivityMyCarsList.this, getString(R.string.main_validation_parking_time_body_spot), Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case R.id.edit:
			showCustomDialog();
			myCarsAdapter.notifyDataSetChanged();
			break;
		}
	}

	public void showCustomDialog() {
		// dialog to add new cars
		((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
		LinearLayout ll_Main = new LinearLayout(this);
		LinearLayout ll_Row1 = new LinearLayout(this);
		LinearLayout ll_Row2 = new LinearLayout(this);
		ll_Main.setOrientation(LinearLayout.VERTICAL);
		ll_Row1.setOrientation(LinearLayout.HORIZONTAL);
		final EditText et_Car = new EditText(this);
		et_Car.setSingleLine(true);
		et_Car.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
		TextView tv_Car = new TextView(this);
		tv_Car.setText(getString(R.string.car));
		ll_Row1.addView(tv_Car);
		ll_Row1.addView(et_Car);
		ll_Main.addView(ll_Row1);
		ll_Main.addView(ll_Row2);
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(getString(R.string.add_car));
		alert.setView(ll_Main);
		alert.setCancelable(false);
		alert.setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(et_Car.getWindowToken(), 0);
				boolean exists = false;
				String user = et_Car.getText().toString();
				if (!user.trim().contentEquals("")) {
					Car car = new Car(user);
					for (Car carTemp : cars) {
						if (carTemp.getCnp().trim().contentEquals(user.trim())) {
							exists = true;
							break;
						}
					}
					if (!exists) {
						cars.add(car);
						myCarsAdapter.gotNewData(cars);
						myCarsAdapter.notifyDataSetChanged();
						Tools.setCars(cars, ActivityMyCarsList.this);
						Intent intent = new Intent(ActivityMyCarsList.this, ActivityMain.class);
						startActivity(intent);
						finish();
					} else {
						Toast.makeText(getBaseContext(), getString(R.string.cnp_exists), Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(getBaseContext(), getString(R.string.please_complete), Toast.LENGTH_SHORT).show();
				}
			}
		});
		alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(et_Car.getWindowToken(), 0);
				dialog.dismiss();
			}
		});
		AlertDialog dialog = alert.create();
		dialog.show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (myTimer != null) {
			myTimer.cancel();
		}
	}

    @SuppressWarnings("NullableProblems")
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Tools.setCars(cars, ActivityMyCarsList.this);
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			ActivityMyCarsList.this.startActivity(new Intent(ActivityMyCarsList.this, ActivityMain.class));
			ActivityMyCarsList.this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

}
