package org.asteroidos.sync.ble.messagetypes;

import java.io.Serializable;

public class EventBusMsg implements Serializable {
    public MessageType messageType = MessageType.STRING;
    public Object messageObject = null;

    public EventBusMsg(MessageType messageType, Object messageObject) {
        this.messageType = messageType;
        this.messageObject = messageObject;
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        throw new java.io.NotSerializableException("org.asteroidos.sync.ble.messagetypes.EventBusMsg");
    }

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException("org.asteroidos.sync.ble.messagetypes.EventBusMsg");
    }

    public enum MessageType {
        STRING, NOTIFICATION, BATTERY
    }

}
