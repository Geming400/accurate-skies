package fr.geming400.accuratedaynightcycle2.config;

import fr.geming400.accuratedaynightcycle2.AccurateDayNightCycle;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.*;

import java.time.DateTimeException;
import java.time.ZoneOffset;

@Modmenu(modId = AccurateDayNightCycle.MOD_ID)
@Config(name = "adnc-config", wrapperName = "ModConfig")
@SuppressWarnings("unused")
public class ModConfigModel {
    public boolean useGeolocalisation = false;
    @PredicateConstraint("checkIfGeolocalisationEnabled")

    @Sync(Option.SyncMode.OVERRIDE_CLIENT)
    @Hook
    public boolean accurateCelestialBodies = true;

    @PredicateConstraint("checkTimezone")
    public String gmtTimeZone = "auto";

    // Internal stuff

    @ExcludeFromScreen
    public String ipAddress = AccurateDayNightCycle.IP_ADDRESS_UNSET;

    @ExcludeFromScreen
    public double latitude = 0;

    @ExcludeFromScreen
    public double longitude = 0;

    public static boolean checkTimezone(String gmtTimeZone) {
        if (gmtTimeZone.equals("auto")) return true;

        try {
            ZoneOffset.of(gmtTimeZone);
            return true;
        } catch (DateTimeException e) {
            return false;
        }
    }

    public static boolean checkIfGeolocalisationEnabled(boolean accurateCelestialBodies) {
        //noinspection ConstantValue
        if (AccurateDayNightCycle.CONFIG == null)
            return true;
        else
            return AccurateDayNightCycle.CONFIG.useGeolocalisation();
    }
}
