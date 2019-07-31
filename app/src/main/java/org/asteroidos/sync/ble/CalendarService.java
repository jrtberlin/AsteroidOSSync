/*
 * Copyright (C) 2019 - Justus Tartz <git@jrtberlin.de>
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

import android.content.Context;
import android.util.Log;

import com.idevicesinc.sweetblue.BleDevice;

import org.asteroidos.sync.utils.CalendarHelper;

import java.util.UUID;

public class CalendarService implements BleDevice.ReadWriteListener {

    private static final UUID calendar_UUID = UUID.fromString("00004071-0000-0000-0000-00A57E401D05");
    private static final UUID calendar_WRT_UUID = UUID.fromString("00004001-0000-0000-0000-00A57E401D05");

    private Context mCtx;
    private BleDevice mDevice;

    public CalendarService(Context ctx, BleDevice device){
        mDevice = device;
        mCtx = ctx;

    }

    @Override
    public void onEvent(ReadWriteEvent e) {
        if (!e.wasSuccess())
            Log.e("CalendarService onEvent", e.status().toString());
    }

    public void sync() {
        updateCalendar();
    }

    public void unsync() {
    }

    private void updateCalendar(){
        getCalendar();
        mDevice.write(calendar_WRT_UUID, calToICS(), CalendarService.this);
    }

    private byte[] calToICS() {
        byte[] ics = null;


        return ics;
    }

    private void getCalendar(){
        CalendarHelper.readCalendar(mCtx);
    }
}
