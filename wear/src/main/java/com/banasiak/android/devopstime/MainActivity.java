package com.banasiak.android.devopstime;

// Most of this code is based on research by...
// Tavon Gatling: https://github.com/kentarosu/AndroidWearAndLIFX
// 9elements: https://github.com/9elements/9elements-One-for-Android-Wear
// These people: http://www.reddit.com/r/androiddev/comments/297r7m/watch_face_development_on_android_wear/
// Also, these people:  https://plus.google.com/communities/113381227473021565406

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
//import com.google.android.clockwork.watchfaces.WatchFaceStyle.Builder;

public class MainActivity extends Activity implements DisplayManager.DisplayListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String TIME_FORMAT_12 = "h:mm";
    private static final String TIME_FORMAT_24 = "H:mm";
    private static final String PERIOD_FORMAT = "a";
    private static final String TIMEZONE_FORMAT = "zzz";
    private static final String DATESTAMP_FORMAT = "EEE, dd MMM yyyy";
    private static final String TIMESTAMP_FORMAT = "HH:mm:ss Z";

    private final static IntentFilter intentFilter;
    static {
        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
    }

    private BroadcastReceiver timeReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            updateTime();
        }

    };

    private DisplayManager displayManager;

    private LinearLayout main;
    private TextView time;
    private TextView period;
    private TextView timezone;
    private TextView datestamp;
    private TextView timestamp;

    private boolean isDimmed = false;

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override public void run() {
            Date date = new Date();
            TimeZone tz = TimeZone.getDefault();
            updateSeconds(date, tz);
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        main = (LinearLayout) findViewById(R.id.main);
        time = (TextView) findViewById(R.id.time);
        period = (TextView) findViewById(R.id.period);
        timezone = (TextView) findViewById(R.id.timezone);
        datestamp = (TextView) findViewById(R.id.datestamp);
        timestamp = (TextView) findViewById(R.id.timestamp);

        displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        displayManager.registerDisplayListener(this, null);

        registerReceiver(timeReceiver, intentFilter);

        updateTime();

        handler.post(runnable);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(timeReceiver);
        displayManager.unregisterDisplayListener(this);
    }

    @Override public void onDisplayChanged(int displayId) {
        Display display = displayManager.getDisplay(displayId);
        if(display == null) {
            // No display found for this ID, treating this as an onScreenOff() but you could remove this line
            // and swallow this exception quietly. What circumstance means 'there is no display for this id'?
            onScreenOff();
            return;
        }
        switch(display.getState()){
            case Display.STATE_DOZING:
                onScreenDim();
                break;
            case Display.STATE_OFF:
                onScreenOff();
                break;
            default:
                //  Not really sure what to so about Display.STATE_UNKNOWN, so
                //  we'll treat it as if the screen is normal.
                onScreenAwake();
                break;
        }
    }

    @Override public void onDisplayAdded(int displayId) {

    }

    @Override public void onDisplayRemoved(int displayId) {
        onWatchFaceRemoved();

    }

    public void onScreenDim() {
        isDimmed = true;

        int silver = getResources().getColor(R.color.silver);
        int black = getResources().getColor(R.color.black);

        main.setBackground(null);
        main.setBackgroundColor(black);

        time.setTextColor(silver);
        period.setTextColor(silver);
        timezone.setVisibility(View.INVISIBLE);
        datestamp.setVisibility(View.GONE);
        timestamp.setVisibility(View.GONE);

        handler.removeCallbacks(null);
    }

    public void onScreenAwake() {
        isDimmed = false;

        int white = getResources().getColor(R.color.white);

        main.setBackground(getResources().getDrawable(R.drawable.kevlar));

        time.setTextColor(white);
        period.setTextColor(white);
        timezone.setVisibility(View.VISIBLE);
        datestamp.setVisibility(View.VISIBLE);
        timestamp.setVisibility(View.VISIBLE);

        handler.post(runnable);
    }

    public void onScreenOff() {

    }

    public void onWatchFaceRemoved() {

    }

    private void updateTime() {
        Date date = new Date();
        TimeZone tz = TimeZone.getDefault();

        SimpleDateFormat timeSdf;
        if(DateFormat.is24HourFormat(this)) {
            timeSdf = new SimpleDateFormat(TIME_FORMAT_24);
            period.setVisibility(View.INVISIBLE);
        } else {
            timeSdf = new SimpleDateFormat(TIME_FORMAT_12);
            period.setVisibility(View.VISIBLE);
        }

        timeSdf.setTimeZone(tz);
        time.setText(timeSdf.format(date));

        SimpleDateFormat periodSdf = new SimpleDateFormat(PERIOD_FORMAT);
        periodSdf.setTimeZone(tz);
        period.setText(periodSdf.format(date));

        SimpleDateFormat timezoneSdf = new SimpleDateFormat(TIMEZONE_FORMAT);
        timezoneSdf.setTimeZone(tz);
        timezone.setText(tz.getDisplayName(tz.inDaylightTime(date), TimeZone.SHORT,
                Locale.getDefault()));

        SimpleDateFormat datestampSdf = new SimpleDateFormat(DATESTAMP_FORMAT);
        datestampSdf.setTimeZone(tz);
        datestamp.setText(datestampSdf.format(date));
    }

    // this will be called by the runnable thread when the screen is awake
    private void updateSeconds(Date date, TimeZone tz) {
        SimpleDateFormat timestampSdf = new SimpleDateFormat(TIMESTAMP_FORMAT);
        timestampSdf.setTimeZone(tz);
        timestamp.setText(timestampSdf.format(date));
    }
}
