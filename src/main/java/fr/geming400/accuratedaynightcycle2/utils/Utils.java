package fr.geming400.accuratedaynightcycle2.utils;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.time.LocalTime;
import java.time.ZonedDateTime;

public final class Utils {
    private Utils() {}

    public static boolean checkDimension(World world, Identifier dimensionID) {
        // Cursed, but it works
        return world.getDimensionEntry()
                .getKey()
                .map(RegistryKey::getValue)
                .map(id -> id.equals(dimensionID))
                .orElse(false);
    }

    public static long timeToMcTime(long time) {
        // This is the simplified formula
        // Original one that I found:
        //
        // 86400 = 1 real day
        // 24000 - ((time / 86400) * 24000 + 6000
        return -(5 * time)/18 + 18000;
    }
    public static long timeToMcTime(LocalTime time) {
        return timeToMcTime(time.toSecondOfDay());
    }
    public static long timeToMcTime(ZonedDateTime time) {
        return timeToMcTime(time.toLocalTime());
    }
}
