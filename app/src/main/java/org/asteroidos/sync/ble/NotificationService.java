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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.asteroidos.sync.NotificationPreferences;
import org.asteroidos.sync.ble.messagetypes.EventBusMsg;
import org.asteroidos.sync.ble.messagetypes.Notification;
import org.greenrobot.eventbus.EventBus;

import java.util.Objects;

public class NotificationService {

    private Context mCtx;

    private NotificationReceiver mNReceiver;

    public NotificationService(Context ctx) {
        mCtx = ctx;
    }

    public final void sync() {
        //mDevice.enableNotify(notificationFeedbackCharac);

        mNReceiver = new NotificationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("org.asteroidos.sync.NOTIFICATION_LISTENER");
        mCtx.registerReceiver(mNReceiver, filter);

        Intent i = new Intent("org.asteroidos.sync.NOTIFICATION_LISTENER_SERVICE");
        i.putExtra("command", "refresh");
        mCtx.sendBroadcast(i);
    }

    public final void unsync() {
        //mDevice.disableNotify(notificationFeedbackCharac);
        try {
            mCtx.unregisterReceiver(mNReceiver);
        } catch (IllegalArgumentException ignored) {
        }
    }

    static class NotificationReceiver extends BroadcastReceiver {
        @Override
        public final void onReceive(Context context, Intent intent) {
            String event = intent.getStringExtra("event");
            if (Objects.equals(event, "posted")) {
                String packageName = intent.getStringExtra("packageName");
                NotificationPreferences.putPackageToSeen(context, packageName);
                NotificationPreferences.NotificationOption notificationOption =
                        NotificationPreferences.getNotificationPreferenceForApp(context, packageName);
                if (notificationOption == NotificationPreferences.NotificationOption.NO_NOTIFICATIONS)
                    return;

                int id = intent.getIntExtra("id", 0);
                String appName = intent.getStringExtra("appName");
                String appIcon = intent.getStringExtra("appIcon");
                String summary = intent.getStringExtra("summary");
                String body = intent.getStringExtra("body");
                String vibration;
                if (notificationOption == NotificationPreferences.NotificationOption.SILENT_NOTIFICATION)
                    vibration = "none";
                else if (notificationOption == null
                        || notificationOption == NotificationPreferences.NotificationOption.NORMAL_VIBRATION
                        || notificationOption == NotificationPreferences.NotificationOption.DEFAULT)
                    vibration = "normal";
                else if (notificationOption == NotificationPreferences.NotificationOption.STRONG_VIBRATION)
                    vibration = "strong";
                else if (notificationOption == NotificationPreferences.NotificationOption.RINGTONE_VIBRATION)
                    vibration = "ringtone";
                else
                    throw new IllegalArgumentException("Not all options handled");

                if (intent.hasExtra("vibration"))
                    vibration = intent.getStringExtra("vibration");

                Notification notification = new Notification(
                        Notification.MsgType.POSTED,
                        packageName,
                        id,
                        appName,
                        appIcon,
                        summary,
                        body,
                        vibration);

                EventBus.getDefault().post(new EventBusMsg(EventBusMsg.MessageType.NOTIFICATION, notification));
                //mDevice.write(notificationUpdateCharac, data, NotificationService.this);
            } else if (Objects.equals(event, "removed")) {
                int id = intent.getIntExtra("id", 0);


                Notification notification = new Notification(Notification.MsgType.REMOVED, id);
                EventBus.getDefault().post(new EventBusMsg(EventBusMsg.MessageType.NOTIFICATION, notification));
                //mDevice.write(notificationUpdateCharac, data, NotificationService.this);
            }
        }
    }
}
