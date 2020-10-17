package org.asteroidos.sync.dataobjects;

import org.asteroidos.sync.BuildConfig;

import java.util.UUID;

public class BleCharacteristic {
    private String characteristicName;
    private UUID characteristicUUID;
    private boolean writeableCharacteristic;

    public BleCharacteristic(String characteristicName, UUID characteristicUUID, boolean writeableCharacteristic) {
        this.characteristicName = characteristicName;
        this.characteristicUUID = characteristicUUID;
        this.writeableCharacteristic = writeableCharacteristic;
    }

    public String getCharacteristicName() {
        return characteristicName;
    }

    public UUID getCharacteristicUUID() {
        return characteristicUUID;
    }

    public final boolean isWriteableCharacteristic() {
        return this.writeableCharacteristic;
    }

    public final boolean isReadableCharactersitic() {
        return !this.writeableCharacteristic;
    }

    public final void printCharacteristicInfo() {
        if (BuildConfig.DEBUG) {
            System.out.println("BLE Characteristic " + getCharacteristicName() + "\n\tUUID: "
                    + getCharacteristicUUID().toString() +
                    "\n\tis writeable: " + isWriteableCharacteristic());
        }
    }
}
