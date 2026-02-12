package fr.geming400.accuratedaynightcycle2.utils;

import fr.geming400.accuratedaynightcycle2.AccurateDayNightCycle;
import io.wispforest.owo.config.ConfigSynchronizer;
import io.wispforest.owo.config.Option;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    /**
     * Updates the owo config for a given {@linkplain ServerPlayerEntity Player}
     * @param player the {@link ServerPlayerEntity} for which to update its owo config
     */
    public static void updateConfig(ServerPlayerEntity player) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) return;

        try {
            AccurateDayNightCycle.LOGGER.info("hi");
            Method toPacketMethod = ConfigSynchronizer.class.getDeclaredMethod("toPacket", Option.SyncMode.class);
            AccurateDayNightCycle.LOGGER.info("hi 2");
            toPacketMethod.setAccessible(true);
            AccurateDayNightCycle.LOGGER.info("hi 3");
            CustomPayload payload = (CustomPayload) toPacketMethod.invoke(null, Option.SyncMode.OVERRIDE_CLIENT);
            AccurateDayNightCycle.LOGGER.info("hi 4");

            ServerPlayNetworking.send(player, payload);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
