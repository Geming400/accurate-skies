package fr.geming400.accuratedaynightcycle2.config;

import fr.geming400.accuratedaynightcycle2.AccurateDayNightCycle;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.*;

@Modmenu(modId = AccurateDayNightCycle.MOD_ID)
@Config(name = "adnc-config", wrapperName = "ModConfig")
@SuppressWarnings("unused")
public class ModConfigModel {
    @Sync(Option.SyncMode.OVERRIDE_CLIENT)
    public boolean useGeolocalisation = false;

    @Hook
    @Sync(Option.SyncMode.OVERRIDE_CLIENT)
    public boolean accurateCelestialBodies = true;

    // Internal stuff

    @ExcludeFromScreen
    public String ipAddress = AccurateDayNightCycle.IP_ADDRESS_UNSET;

    @ExcludeFromScreen
    public double latitude = 0;

    @ExcludeFromScreen
    public double longitude = 0;
}
