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
    public static final String IP_ADDRESS_UNSET = "DO NOT MODIFY - NOT SET";


    // public boolean useGeolocalisation = false; // TODO: Remove that on publish
    public boolean useGeolocalisation = true;
    @PredicateConstraint("checkTimezone")
    public String gmtTimeZone = "auto";
    @Sync(Option.SyncMode.OVERRIDE_CLIENT)
    public boolean accurateCelestialBodies = true;

    @ExcludeFromScreen
    public String ipAddress = IP_ADDRESS_UNSET;

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
}
