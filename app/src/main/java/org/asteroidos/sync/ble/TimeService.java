/*
 * Copyright (C) 2016 - Florent Revest <revestflo@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.asteroidos.sync.ble;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.SystemClock;

import java.util.Calendar;
import java.util.UUID;

@SuppressWarnings( "deprecation" ) // Before upgrading to SweetBlue 3.0, we don't have an alternative to the deprecated ReadWriteListener
public class TimeService implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final UUID timeSetCharac = UUID.fromString("00005001-0000-0000-0000-00a57e401d05");

    public static final String PREFS_NAME = "TimePreference";
    public static final String PREFS_SYNC_TIME = "syncTime";
    public static final boolean PREFS_SYNC_TIME_DEFAULT = true;
    public static final String TIME_SYNC_INTENT = "org.asteroidos.sync.TIME_SYNC_REQUEST_LISTENER";

    private BluetoothDevice mDevice;
    private Context mCtx;

    private SharedPreferences mTimeSyncSettings;

    private TimeSyncReqReceiver mSReceiver;
    private PendingIntent alarmPendingIntent;
    private AlarmManager alarmMgr;

    public TimeService(Context ctx, BluetoothDevice device) {
        mDevice = device;
        mCtx = ctx;
        mTimeSyncSettings = ctx.getSharedPreferences(PREFS_NAME, 0);
        mTimeSyncSettings.registerOnSharedPreferenceChangeListener(this);
    }

    public void sync() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateTime();
            }
        }, 500);

        // Register a broadcast handler to use for the alarm Intent
        // Also listen for TIME_CHANGED and TIMEZONE_CHANGED events
        mSReceiver = new TimeSyncReqReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(TIME_SYNC_INTENT);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        mCtx.registerReceiver(mSReceiver, filter);

        // register an alarm to sync the time once a day
        Intent alarmIntent = new Intent(TIME_SYNC_INTENT);
        alarmPendingIntent = PendingIntent.getBroadcast(mCtx, 0, alarmIntent, 0);
        alarmMgr = (AlarmManager) mCtx.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_DAY,
                AlarmManager.INTERVAL_DAY, alarmPendingIntent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateTime();
    }

    private void updateTime() {
        if(mTimeSyncSettings.getBoolean(PREFS_SYNC_TIME, PREFS_SYNC_TIME_DEFAULT)) {
            byte[] data = new byte[6];
            Calendar c =  Calendar.getInstance();
            data[0] = (byte)(c.get(Calendar.YEAR) - 1900);
            data[1] = (byte)(c.get(Calendar.MONTH));
            data[2] = (byte)(c.get(Calendar.DAY_OF_MONTH));
            data[3] = (byte)(c.get(Calendar.HOUR_OF_DAY));
            data[4] = (byte)(c.get(Calendar.MINUTE));
            data[5] = (byte)(c.get(Calendar.SECOND));
            //mDevice.write(timeSetCharac, data, TimeService.this); TODO: new lib call
        }
    }

    public void unsync() {
        try {
            mCtx.unregisterReceiver(mSReceiver);
        } catch (IllegalArgumentException ignored) {}
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmPendingIntent);
        }
    }

    class TimeSyncReqReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTime();
        }
    }
}
