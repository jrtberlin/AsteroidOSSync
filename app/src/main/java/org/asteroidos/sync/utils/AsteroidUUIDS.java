/* AsteroidOS UUID collection for ble characteristics and watch filtering */
package org.asteroidos.sync.utils;

import java.util.UUID;

public class AsteroidUUIDS {
    //AsteroidOS Service Watch Filter UUID
    public static final UUID SERVICE_UUID               = UUID.fromString("00000000-0000-0000-0000-00A57E401D05");

    //Battery level
    public static final UUID BATTERY_SERVICE_UUID       = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB");
    public static final UUID BATTERY_UUID               = UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB");

    //ScreenshotService
    public static final UUID screenshotRequestCharac    = UUID.fromString("00006001-0000-0000-0000-00A57E401D05");
    public static final UUID screenshotContentCharac    = UUID.fromString("00006002-0000-0000-0000-00A57E401D05");

    //MediaService
    public static final UUID mediaTitleCharac           = UUID.fromString("00007001-0000-0000-0000-00A57E401D05");
    public static final UUID mediaAlbumCharac           = UUID.fromString("00007002-0000-0000-0000-00A57E401D05");
    public static final UUID mediaArtistCharac          = UUID.fromString("00007003-0000-0000-0000-00A57E401D05");
    public static final UUID mediaPlayingCharac         = UUID.fromString("00007004-0000-0000-0000-00A57E401D05");
    public static final UUID mediaCommandsCharac        = UUID.fromString("00007005-0000-0000-0000-00A57E401D05");
    public static final UUID mediaVolumeCharac          = UUID.fromString("00007006-0000-0000-0000-00A57E401D05");

    // WeatherService
    public static final UUID weatherCityCharac          = UUID.fromString("00008001-0000-0000-0000-00A57E401D05");
    public static final UUID weatherIdsCharac           = UUID.fromString("00008002-0000-0000-0000-00A57E401D05");
    public static final UUID weatherMinTempsCharac      = UUID.fromString("00008003-0000-0000-0000-00A57E401D05");
    public static final UUID weatherMaxTempsCharac      = UUID.fromString("00008004-0000-0000-0000-00A57E401D05");

    //Notification Service
    public static final UUID notificationUpdateCharac   = UUID.fromString("00009001-0000-0000-0000-00A57E401D05");
    public static final UUID notificationFeedbackCharac = UUID.fromString("00009002-0000-0000-0000-00A57E401D05");
}
