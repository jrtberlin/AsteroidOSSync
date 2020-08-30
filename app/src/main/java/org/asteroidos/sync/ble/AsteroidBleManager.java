package org.asteroidos.sync.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.asteroidos.sync.utils.AsteroidUUIDS;
import org.greenrobot.eventbus.EventBus;

import java.util.Objects;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.BuildConfig;
import no.nordicsemi.android.ble.data.Data;

public class AsteroidBleManager extends BleManager {

    @Nullable
    public BluetoothGattCharacteristic batteryCharacteristic;

    public AsteroidBleManager(@NonNull final Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected final BleManagerGattCallback getGattCallback() {
        return new AsteroidBleManagerGattCallback();
    }

    @Override
    public void log(final int priority, @NonNull final String message) {
        if (BuildConfig.DEBUG || priority == Log.ERROR) {
            Log.println(priority, "MyBleManager", message);
        }
    }

    public final void abort() {
        cancelQueue();
    }

    public final void setBatteryLevel(Data data) {
        System.out.println("DEBUG BATTERY: " + data.getByte(0) + "%");
        BatteryLevelEvent batteryLevelEvent = new BatteryLevelEvent();
        batteryLevelEvent.battery = Objects.requireNonNull(data.getByte(0)).intValue();
        EventBus.getDefault().post(batteryLevelEvent);
    }

    public static class BatteryLevelEvent {
        public int battery = 0;
    }

    private class AsteroidBleManagerGattCallback extends BleManagerGattCallback {

        @Override
        public final boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(AsteroidUUIDS.BATTERY_SERVICE_UUID);
            boolean notify = false;
            if (service != null) {
                batteryCharacteristic = service.getCharacteristic(AsteroidUUIDS.BATTERY_UUID);

                if (batteryCharacteristic != null) {
                    final int properties = batteryCharacteristic.getProperties();
                    notify = (properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
                }
            }
            // Return true if all required services have been found
            return (batteryCharacteristic != null && notify);
        }

        @Override
        protected final boolean isOptionalServiceSupported(@NonNull final BluetoothGatt gatt) {
            return super.isOptionalServiceSupported(gatt);
        }

        @Override
        protected final void initialize() {
            beginAtomicRequestQueue()
                    .add(requestMtu(256) // Remember, GATT needs 3 bytes extra. This will allow packet size of 244 bytes.
                            .with((device, mtu) -> log(Log.INFO, "MTU set to " + mtu))
                            .fail((device, status) -> log(Log.WARN, "Requested MTU not supported: " + status)))
                    .done(device -> log(Log.INFO, "Target initialized"))
                    .fail((device, status) -> Log.e("Init", device.getName() + " not initialized with error: " + status))
                    .enqueue();

            setNotificationCallback(batteryCharacteristic).with(((device, data) -> setBatteryLevel(data)));
            readCharacteristic(batteryCharacteristic).with(((device, data) -> setBatteryLevel(data)));
            enableNotifications(batteryCharacteristic).enqueue();
        }

        @Override
        protected final void onDeviceDisconnected() {
            batteryCharacteristic = null;
        }

    }

}