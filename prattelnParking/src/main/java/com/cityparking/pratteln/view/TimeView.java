package com.cityparking.pratteln.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.view.View;

import com.cityparking.pratteln.R;
import com.cityparking.pratteln.utils.LogService;

public class TimeView extends View { // this is the clock that is used in activity remaining time

	Context context;
	float percent = 0;
	float ratio;
	int displayWidth, displayHeight;
	float density;
	private PorterDuffXfermode pd;
	private RectF rectF;
	private Paint paint;
	public long time;
	private boolean red;

	public TimeView(Context mContext) { // initiates the timeview
		super(mContext);
		context = mContext;
		displayWidth = getResources().getDisplayMetrics().widthPixels;
		displayHeight = getResources().getDisplayMetrics().heightPixels;
		density = getResources().getDisplayMetrics().density;
		LogService.log("", "density" + density + " | W: " + displayWidth + " | H : " + displayHeight);
		pd = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
		float middle = displayWidth / 2;
		rectF = new RectF(middle / 2 - 32 * density, middle / 2 + 10 * density, middle + middle / 2 + 32 * density, middle + middle / 2 + 74 * density);
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);

	}

	protected void onDraw(Canvas canvas) { // on draw function will draw the clock
		super.onDraw(canvas);
		paint.setColor(getResources().getColor(R.color.timer_clock));
		paint.setAlpha(200);
		paint.setStrokeWidth(2.0f);
		int circleRadius = (int) (110 * density);
		if (density == 1) {
			circleRadius = (int) (80 * density);
		} else if (density == 1.5) {
			circleRadius = (int) (95 * density);
		}
		paint.setStyle(Paint.Style.FILL);
		canvas.drawCircle(rectF.centerX(), rectF.centerY(), circleRadius, paint);
		paint.setStyle(Paint.Style.FILL);
		if (android.os.Build.VERSION.SDK_INT > 10) {
			paint.setColor(Color.TRANSPARENT);
			paint.setAlpha(0);
			paint.setXfermode(pd);
		} else {
			paint.setColor(0xFFA3A187);
		}
		canvas.drawArc(rectF, -90, 360 - percent, true, paint);
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(Color.BLACK);
		paint.setXfermode(null);
		paint.setAlpha(255);
		paint.setStyle(Paint.Style.FILL);
		paint.setTextSize(25 * density);
		// will take timestamp, transform into String of: hh:mm:ss and draw it
		if (time > -1) {
			paint.setColor(Color.BLACK);
			if (red) {
				paint.setColor(Color.RED);
			}
			int hours = (int) (time / (60 * 60 * 1000));
			int hoursMod = (int) (time % (60 * 60 * 1000));
			int min = (int) (hoursMod / (1000 * 60));
			int minMod = (int) (hoursMod % (1000 * 60));
			int seconds = (int) (minMod / 1000);
			if (hours != 0) {
				String timeS = hours + "h " + min + "m " + seconds + "s ";
				drawDigit(canvas, (int) (25 * density), rectF.centerX(), rectF.centerY(), paint.getColor(), timeS);
				// canvas.drawText(timeS, rectF.left, rectF.centerY(), paint);
			} else if (min != 0) {
				String timeS = min + "m " + seconds + "s ";
				drawDigit(canvas, (int) (25 * density), rectF.centerX(), rectF.centerY(), paint.getColor(), timeS);
				// canvas.drawText(timeS, rectF.centerX() - (timeS.length()), rectF.centerY(), paint);
			} else {
				String timeS = seconds + "s ";
				drawDigit(canvas, (int) (25 * density), rectF.centerX(), rectF.centerY(), paint.getColor(), timeS);
				// canvas.drawText(timeS, rectF.centerX() - (timeS.length()), rectF.centerY(), paint);
			}
		} else {
			paint.setColor(Color.RED); // will set the text on "finished" string
			String timeS = context.getString(R.string.finished);
			drawDigit(canvas, (int) (25 * density), rectF.centerX(), rectF.centerY(), paint.getColor(), timeS);
		}
		invalidate();
	}

	public void setTime(long millisUntilFinished) { // will set the time using the ratio to know, how much of the circle to show
		percent = millisUntilFinished * ratio;
		// LogService.log("", "percent: " + percent);
		invalidate();
	}

	public void setFullTime(long timeToCount) { // will set the ratio by setting the full time on the 360 degrees
		ratio = (float) (360f / timeToCount);
		LogService.log("", "ratio: " + ratio);
	}

	public void setTimeForText(long time, boolean red) { // will set the time for the time text inside the clock
		this.time = time;
		this.red = red;
	}

	private void drawDigit(Canvas canvas, int textSize, float cX, float cY, int color, String text) { // will draw the digits
		Paint tempTextPaint = new Paint();
		tempTextPaint.setAntiAlias(true);
		tempTextPaint.setStyle(Paint.Style.FILL);

		tempTextPaint.setColor(color);
		tempTextPaint.setTextSize(textSize);

		float textWidth = tempTextPaint.measureText(text);
		// if cX and cY are the origin coordinates of the your rectangle
		// cX-(textWidth/2) = The x-coordinate of the origin of the text being drawn
		// cY+(textSize/2) = The y-coordinate of the origin of the text being drawn

		canvas.drawText(text, cX - (textWidth / 2), cY + (textSize / 2), tempTextPaint);
	}

}
