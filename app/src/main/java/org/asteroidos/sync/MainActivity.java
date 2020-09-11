package org.asteroidos.sync;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelUuid;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.view.MenuItem;

import org.asteroidos.sync.ble.AsteroidBleManager;
import org.asteroidos.sync.ble.messagetypes.EventBusMsg;
import org.asteroidos.sync.fragments.AppListFragment;
import org.asteroidos.sync.fragments.DeviceDetailFragment;
import org.asteroidos.sync.fragments.DeviceListFragment;
import org.asteroidos.sync.fragments.PositionPickerFragment;
import org.asteroidos.sync.services.SynchronizationService;
import org.asteroidos.sync.utils.AppInfo;
import org.asteroidos.sync.utils.AppInfoHelper;
import org.asteroidos.sync.utils.AsteroidUUIDS;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

import static android.os.ParcelUuid.fromString;

public class MainActivity extends AppCompatActivity implements DeviceListFragment.OnDefaultDeviceSelectedListener,
        DeviceListFragment.OnScanRequestedListener, DeviceDetailFragment.OnDefaultDeviceUnselectedListener,
        DeviceDetailFragment.OnConnectRequestedListener,
        DeviceDetailFragment.OnAppSettingsClickedListener, DeviceDetailFragment.OnLocationSettingsClickedListener,
        DeviceDetailFragment.OnUpdateListener {
    private DeviceListFragment mListFragment;
    private DeviceDetailFragment mDetailFragment;
    private Fragment mPreviousFragment;
    Messenger mSyncServiceMessenger;
    Intent mSyncServiceIntent;
    final Messenger mDeviceDetailMessenger = new Messenger(new MainActivity.SynchronizationHandler(this));
    int mStatus = SynchronizationService.STATUS_DISCONNECTED;

    public static ArrayList<AppInfo> appInfoList;

    public static final String PREFS_NAME = "MainPreferences";
    public static final String PREFS_DEFAULT_MAC_ADDR = "defaultMacAddress";
    public static final String PREFS_DEFAULT_LOC_NAME = "defaultLocalName";
    public ParcelUuid asteroidUUID = fromString(AsteroidUUIDS.SERVICE_UUID.toString());

    private BluetoothLeScannerCompat scanner;
    ScanSettings settings;
    List<ScanFilter> filters;
    private SharedPreferences mPrefs;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onMessageEvent(EventBusMsg eventBusMsg) {
        if (eventBusMsg.messageType == EventBusMsg.MessageType.BATTERY)
            handleBatteryPercentage(((AsteroidBleManager.BatteryLevelEvent) eventBusMsg.messageObject).battery);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_main);

        scanner = BluetoothLeScannerCompat.getScanner();

        mPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String defaultDevMacAddr = mPrefs.getString(PREFS_DEFAULT_MAC_ADDR, "");

        Thread appInfoRetrieval = new Thread(new Runnable() {
            public void run() {
                appInfoList = AppInfoHelper.getPackageInfo(MainActivity.this);
            }
        });
        appInfoRetrieval.start();

        settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(asteroidUUID).build());

        scanner.startScan(filters, settings, scanCallback);

        /* Start and/or attach to the Synchronization Service */
        mSyncServiceIntent = new Intent(this, SynchronizationService.class);
        startService(mSyncServiceIntent);

        if (savedInstanceState == null) {
            Fragment f;
            if (defaultDevMacAddr.isEmpty()) {
                f = mListFragment = new DeviceListFragment();
                onScanRequested();
            } else {
                setTitle(mPrefs.getString(PREFS_DEFAULT_LOC_NAME, ""));
                f = mDetailFragment = new DeviceDetailFragment();
            }

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.flContainer, f);
            ft.commit();
        }
    }

    public ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, @NonNull ScanResult result) {
            super.onScanResult(callbackType, result);

            if (mListFragment == null) return;
            mListFragment.deviceDiscovered(result.getDevice());
            if(BuildConfig.DEBUG)
                System.out.println("SCAN RESULT:" + result.getDevice().toString() + " Name:" + result.getDevice().getName());
            ParcelUuid[] arr = result.getDevice().getUuids();
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if(mStatus != SynchronizationService.STATUS_CONNECTED)
            stopService(mSyncServiceIntent);
    }

    /* Fragments switching */
    @Override
    public void onDefaultDeviceSelected(BluetoothDevice mDevice) {
        scanner.stopScan(scanCallback);
        mListFragment.scanningStopped();
        mDetailFragment = new DeviceDetailFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flContainer, mDetailFragment)
                .commit();

        try {
            Message msg = Message.obtain(null, SynchronizationService.MSG_SET_DEVICE);
            msg.obj = mDevice;
            msg.replyTo = mDeviceDetailMessenger;
            mSyncServiceMessenger.send(msg);
        } catch (RemoteException ignored) {}

        onConnectRequested();

        EventBus.getDefault().register(this);
        mListFragment = null;
    }

    @Override
    public void onDefaultDeviceUnselected() {
        mListFragment = new DeviceListFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flContainer, mListFragment)
                .commit();

        try {
            Message msg = Message.obtain(null, SynchronizationService.MSG_UNSET_DEVICE);
            msg.replyTo = mDeviceDetailMessenger;
            mSyncServiceMessenger.send(msg);
        } catch (RemoteException ignored) {}

        mDetailFragment = null;
        EventBus.getDefault().unregister(this);
        setTitle(R.string.app_name);
    }

    @Override
    public void onUpdateRequested() {
        try {
            Message msg = Message.obtain(null, SynchronizationService.MSG_UPDATE);
            msg.replyTo = mDeviceDetailMessenger;
            if(mSyncServiceMessenger != null)
                mSyncServiceMessenger.send(msg);
        } catch (RemoteException ignored) {}
    }

    /* Synchronization service events handling */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            mSyncServiceMessenger = new Messenger(service);
            onUpdateRequested();
        }

        public void onServiceDisconnected(ComponentName className) {
            mSyncServiceMessenger = null;
        }
    };

    @Override
    public void onConnectRequested() {
        if(scanner != null)
            scanner.stopScan(scanCallback);
        try {
            Message msg = Message.obtain(null, SynchronizationService.MSG_CONNECT);
            msg.replyTo = mDeviceDetailMessenger;
            mSyncServiceMessenger.send(msg);
        } catch (RemoteException ignored) {}
    }

    @Override
    public void onDisconnectRequested() {
        try {
            Message msg = Message.obtain(null, SynchronizationService.MSG_DISCONNECT);
            msg.replyTo = mDeviceDetailMessenger;
            mSyncServiceMessenger.send(msg);
        } catch (RemoteException ignored) {}
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if(menuItem.getItemId() ==  android.R.id.home)
            onBackPressed();

        return (super.onOptionsItemSelected(menuItem));
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if(fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
            setTitle(mPrefs.getString(PREFS_DEFAULT_LOC_NAME, ""));
            ActionBar ab = getSupportActionBar();
            if (ab != null)
                ab.setDisplayHomeAsUpEnabled(false);
        } else
            finish();
        try {
            mDetailFragment = (DeviceDetailFragment)mPreviousFragment;
        } catch (ClassCastException ignored1) {
            try {
                mListFragment = (DeviceListFragment)mPreviousFragment;
            } catch (ClassCastException ignored2) {}
        }
    }

    @Override
    public void onAppSettingsClicked() {
        Fragment f = new AppListFragment();

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (mDetailFragment != null) {
            mPreviousFragment = mDetailFragment;
            mDetailFragment = null;
        }
        if (mListFragment != null) {
            mPreviousFragment = mListFragment;
            mListFragment = null;
        }
        ft.replace(R.id.flContainer, f);
        ft.addToBackStack(null);
        ft.commit();

        setTitle(getString(R.string.notifications_settings));
        ActionBar ab = getSupportActionBar();
        if (ab != null)
            ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onLocationSettingsClicked() {
        Fragment f = new PositionPickerFragment();

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (mDetailFragment != null) {
            mPreviousFragment = mDetailFragment;
            mDetailFragment = null;
        }
        if (mListFragment != null) {
            mPreviousFragment = mListFragment;
            mListFragment = null;
        }
        ft.replace(R.id.flContainer, f);
        ft.addToBackStack(null);
        ft.commit();

        setTitle(getString(R.string.weather_settings));
        ActionBar ab = getSupportActionBar();
        if (ab != null)
            ab.setDisplayHomeAsUpEnabled(true);
    }

    private void handleSetLocalName(String name) {
        if(mDetailFragment != null)
            mDetailFragment.setLocalName(name);
    }

    private void handleSetStatus(int status) {
        if(mDetailFragment != null) {
            mDetailFragment.setStatus(status);
            if(status == SynchronizationService.STATUS_CONNECTED) {
                try {
                    Message batteryMsg = Message.obtain(null, SynchronizationService.MSG_REQUEST_BATTERY_LIFE);
                    batteryMsg.replyTo = mDeviceDetailMessenger;
                    mSyncServiceMessenger.send(batteryMsg);
                } catch (RemoteException ignored) {}
            }
            mStatus = status;
        }
    }

    private void handleBatteryPercentage(int percentage) {
        if(mDetailFragment != null)
            mDetailFragment.setBatteryPercentage(percentage);
    }

    static private class SynchronizationHandler extends Handler {
        private MainActivity mActivity;

        SynchronizationHandler(MainActivity activity) {
            mActivity = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SynchronizationService.MSG_SET_LOCAL_NAME:
                    mActivity.handleSetLocalName((String)msg.obj);
                    break;
                case SynchronizationService.MSG_SET_STATUS:
                    mActivity.handleSetStatus(msg.arg1);
                    break;
                case SynchronizationService.MSG_SET_BATTERY_PERCENTAGE:
                    System.out.println("Got battery percentage by callback: " + msg.arg1);
                    mActivity.handleBatteryPercentage(msg.arg1);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    public void onScanRequested() {
        //scanner.flushPendingScanResults(scanCallback); Todo: fix crash on subsequent call
        scanner.stopScan(scanCallback);
        scanner.startScan(filters, settings, scanCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(mSyncServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getDelegate().onConfigurationChanged(newConfig);
        int currentNightMode = newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }

        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
    }
}
