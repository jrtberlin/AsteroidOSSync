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

package org.asteroidos.sync.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import org.asteroidos.sync.MainActivity;
import org.asteroidos.sync.R;
import org.asteroidos.sync.ble.AsteroidBleManager;
import org.asteroidos.sync.ble.MediaService;
import org.asteroidos.sync.ble.NotificationService;
import org.asteroidos.sync.ble.ScreenshotService;
import org.asteroidos.sync.ble.SilentModeService;
import org.asteroidos.sync.ble.TimeService;
import org.asteroidos.sync.ble.WeatherService;

import no.nordicsemi.android.ble.observer.ConnectionObserver;

public class SynchronizationService extends Service implements ConnectionObserver {
    public static final int MSG_CONNECT = 1;
    public static final int MSG_DISCONNECT = 2;
    public static final int MSG_SET_LOCAL_NAME = 3;
    public static final int MSG_SET_STATUS = 4;
    public static final int MSG_SET_BATTERY_PERCENTAGE = 5;
    public static final int MSG_REQUEST_BATTERY_LIFE = 6;
    public static final int MSG_SET_DEVICE = 7;
    public static final int MSG_UPDATE = 8;
    public static final int MSG_UNSET_DEVICE = 9;
    public static final int STATUS_CONNECTED = 1;
    public static final int STATUS_DISCONNECTED = 2;
    public static final int STATUS_CONNECTING = 3;
    private static final String NOTIFICATION_CHANNEL_ID = "synchronizationservice_channel_id_01";
    public static BluetoothDevice mDevice;
    final Messenger mMessenger = new Messenger(new SynchronizationHandler(this));
    public BluetoothGatt gatt;
    private NotificationManager mNM;
    private int NOTIFICATION = 2725;
    private AsteroidBleManager mBleMngr;
    private int mState = STATUS_DISCONNECTED;
    private Messenger replyTo;
    private ScreenshotService mScreenshotService;
    private WeatherService mWeatherService;
    private NotificationService mNotificationService;
    private MediaService mMediaService;
    private TimeService mTimeService;
    private SilentModeService silentModeService;
    private SharedPreferences mPrefs;

    void handleConnect() {
        if (mBleMngr == null) return;
        if (mState == STATUS_CONNECTED || mState == STATUS_CONNECTING) return;

        mPrefs = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
        String defaultDevMacAddr = mPrefs.getString(MainActivity.PREFS_DEFAULT_MAC_ADDR, "");
        String defaultLocalName = mPrefs.getString(MainActivity.PREFS_DEFAULT_LOC_NAME, "");
        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(defaultDevMacAddr);


        mBleMngr.setConnectionObserver(this);
        device.createBond();
        mBleMngr.connect(device)
                .timeout(100000)
                .retry(3, 200)
                .done(device1 -> {
                    Log.d("BLE Connect", "Connected to " + device1.getName());
                    mWeatherService = new WeatherService(getApplicationContext(), device1);
                    mDevice = device1;

                })
                .fail((device2, error) -> {
                    Log.e("BLE Connect", "Failed to connect to " + device.getName()
                            + " with error code: " + error);
                })
                .enqueue();


        /*mWeatherService = new WeatherService(getApplicationContext(), mDevice);
        mNotificationService = new NotificationService(getApplicationContext(), mDevice);
        mMediaService = new MediaService(getApplicationContext(), mDevice);
        mScreenshotService = new ScreenshotService(getApplicationContext(), mDevice);
        mTimeService = new TimeService(getApplicationContext(), mDevice);*/
        silentModeService = new SilentModeService(getApplicationContext());

        //mDevice.connect();

    }

    void handleDisconnect() {
        if (mBleMngr == null) return;
        if (mState == STATUS_DISCONNECTED) return;
        /*mScreenshotService.unsync();
        mWeatherService.unsync();
        mNotificationService.unsync();
        mMediaService.unsync();
        mTimeService.unsync();*/
        mBleMngr.abort();
        mBleMngr.disconnect().enqueue();
        silentModeService.onDisconnect();
    }

    void handleSetDevice(BluetoothDevice device) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(MainActivity.PREFS_DEFAULT_MAC_ADDR, device.getAddress());

        mDevice = device;

        String name = mDevice.getName();
        try {
            Message answer = Message.obtain(null, MSG_SET_LOCAL_NAME);
            answer.obj = name;
            replyTo.send(answer);

            replyTo.send(Message.obtain(null, MSG_SET_STATUS, mState, 0));
        } catch (RemoteException | NullPointerException ignored) {
        }

        editor.putString(MainActivity.PREFS_DEFAULT_LOC_NAME, name);
        editor.apply();
    }

    void handleUpdate() {
        if (mDevice != null) {
            try {
                replyTo.send(Message.obtain(null, MSG_SET_STATUS, mState, 0));
            } catch (RemoteException | NullPointerException ignored) {
            }
        }
    }

    @Override
    public void onDeviceConnecting(@NonNull BluetoothDevice device) {
        mState = STATUS_CONNECTING;
        updateNotification();
    }

    @Override
    public void onDeviceConnected(@NonNull BluetoothDevice device) {
        mState = STATUS_CONNECTED;
        updateNotification();

    }

    @Override
    public void onDeviceFailedToConnect(@NonNull BluetoothDevice device, int reason) {

    }

    @Override
    public void onDeviceReady(@NonNull BluetoothDevice device) {
        mState = STATUS_CONNECTED;
        updateNotification();
    }

    @Override
    public void onDeviceDisconnecting(@NonNull BluetoothDevice device) {
        mState = STATUS_DISCONNECTED;
        updateNotification();

    }

    @Override
    public void onDeviceDisconnected(@NonNull BluetoothDevice device, int reason) {
        mState = STATUS_DISCONNECTED;
        updateNotification();
    }

    private void handleUnSetDevice() {
        SharedPreferences.Editor editor = mPrefs.edit();
        if (mState != STATUS_DISCONNECTED) {
            /*mScreenshotService.unsync();
            mWeatherService.unsync();
            mNotificationService.unsync();
            mMediaService.unsync();
            mTimeService.unsync();*/
            mBleMngr.disconnect().enqueue();
        }
        mDevice = null;
        editor.putString(MainActivity.PREFS_DEFAULT_LOC_NAME, "");
        editor.putString(MainActivity.PREFS_DEFAULT_MAC_ADDR, "");
        editor.putString(MainActivity.PREFS_NAME, "");
        editor.apply();
    }

    @Override
    public void onCreate() {
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Synchronization Service", NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setDescription("Connection status");
            notificationChannel.setVibrationPattern(new long[]{0L});
            notificationChannel.setShowBadge(false);
            mNM.createNotificationChannel(notificationChannel);
        }

        //TODO setup ble lib
        mBleMngr = new AsteroidBleManager(getApplicationContext());

        mPrefs = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
        String defaultDevMacAddr = mPrefs.getString(MainActivity.PREFS_DEFAULT_MAC_ADDR, "");
        String defaultLocalName = mPrefs.getString(MainActivity.PREFS_DEFAULT_LOC_NAME, "");

        if (!defaultDevMacAddr.isEmpty()) {
            /*
            if(!mBleMngr.hasDevice(defaultDevMacAddr))
                mBleMngr.newDevice(defaultDevMacAddr, defaultLocalName);

            mDevice = mBleMngr.getDevice(defaultDevMacAddr);
            mDevice.setListener_State(SynchronizationService.this);

             */
            mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(defaultDevMacAddr);

            /*
            mWeatherService = new WeatherService(getApplicationContext(), mDevice);
            mNotificationService = new NotificationService(getApplicationContext(), mDevice);
            mMediaService = new MediaService(getApplicationContext(), mDevice);
            mScreenshotService = new ScreenshotService(getApplicationContext(), mDevice);
            mTimeService = new TimeService(getApplicationContext(), mDevice);
            silentModeService = new SilentModeService(getApplicationContext());
             */

            mBleMngr.connect(mDevice);
        }

        updateNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void updateNotification() {
        handleUpdate();
        String status = getString(R.string.disconnected);
        if (mDevice != null) {
            if (mState == STATUS_CONNECTING)
                status = getString(R.string.connecting_formatted, mDevice.getName());
            else if (mState == STATUS_CONNECTED)
                status = getString(R.string.connected_formatted, mDevice.getName());
        }

        if (mDevice != null) {
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setContentTitle(getText(R.string.app_name))
                    .setContentText(status)
                    .setContentIntent(contentIntent)
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_MIN)
                    .setShowWhen(false)
                    .build();

            mNM.notify(NOTIFICATION, notification);
            startForeground(NOTIFICATION, notification);
        }
    }

    @Override
    public void onDestroy() {
        if (mDevice != null)
            mDevice = null;
        mBleMngr.disconnect();
        mNM.cancel(NOTIFICATION);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    static private class SynchronizationHandler extends Handler {
        private SynchronizationService mService;

        SynchronizationHandler(SynchronizationService service) {
            mService = service;
        }

        @Override
        public void handleMessage(Message msg) {
            mService.replyTo = msg.replyTo;

            switch (msg.what) {
                case MSG_CONNECT:
                    mService.handleConnect();
                    break;
                case MSG_DISCONNECT:
                    mService.handleDisconnect();
                    break;
                case MSG_REQUEST_BATTERY_LIFE:
                    //mService.handleReqBattery();
                    break;
                case MSG_SET_DEVICE:
                    mService.handleSetDevice((BluetoothDevice) msg.obj);
                    break;
                case MSG_UNSET_DEVICE:
                    mService.handleUnSetDevice();
                    break;
                case MSG_UPDATE:
                    mService.handleUpdate();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

}
