package org.asteroidos.sync.dataobjects;

import androidx.annotation.Nullable;

import java.util.UUID;

public class BleService {
    private final String serviceName;
    private final UUID serviceUUID;
    @Nullable
    private BleCharacteristic[] serviceCharacteristics;

    /***
     *
     * @param serviceName Name of the Service to display in debug functions
     * @param serviceUUID {@link UUID} defining the BLE service
     * @param serviceCharacteristics Array of {@link BleCharacteristic} that the service consists of
     */
    public BleService(String serviceName, UUID serviceUUID, @Nullable BleCharacteristic[] serviceCharacteristics) {
        this.serviceName = serviceName;
        this.serviceUUID = serviceUUID;
        this.serviceCharacteristics = serviceCharacteristics;
    }

    public BleService(String serviceName, UUID serviceUUID) {
        this.serviceName = serviceName;
        this.serviceUUID = serviceUUID;
        this.serviceCharacteristics = null;
    }

    public final void addCharacteristic(BleCharacteristic bleCharacteristic) {
        int size = 1;
        if (this.serviceCharacteristics != null) {
            size = this.serviceCharacteristics.length + 1;
        }

        BleCharacteristic[] returnCharacteristic = new BleCharacteristic[size];
        int i = 0;
        if (this.serviceCharacteristics != null) {
            for (BleCharacteristic characteristic : this.serviceCharacteristics) {
                returnCharacteristic[i] = characteristic;
                i += 1;
            }
        }
        returnCharacteristic[i] = bleCharacteristic;
        this.serviceCharacteristics = returnCharacteristic;
    }

    public final void addCharacteristics(BleCharacteristic[] characteristics) {
        for (BleCharacteristic characteristic : characteristics) {
            addCharacteristic(characteristic);
        }
    }

    public final String getServiceName() {
        return serviceName;
    }

    public final UUID getServiceUUID() {
        return serviceUUID;
    }

    @Nullable
    public final BleCharacteristic[] getServiceCharacteristics() {
        return serviceCharacteristics;
    }
}
