package fr.geming400.accuratedaynightcycle2;

import fr.geming400.accuratedaynightcycle2.utils.IpUtils;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class ServerEvents {
    private ServerEvents() {}

    public static void initialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(mcServer -> {
            AccurateDayNightCycle.server = mcServer;
            AccurateDayNightCycle.loadOrCreateDb(false);
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(mcServer -> {
            AccurateDayNightCycle.server = null;
            AccurateDayNightCycle.playersWithMod.clear();
        });

        ServerPlayerEvents.JOIN.register(player -> {
            if (AccurateDayNightCycle.CONFIG.accurateCelestialBodies()) {
                AccurateDayNightCycle.LOGGER.info("Sending geolocation infos to player {}", player.getName().getString());
                IpUtils.Geolocation.fromConfig().updatePlayerGeolocation(player);
            }
        });

        ServerPlayerEvents.LEAVE.register(player ->
                AccurateDayNightCycle.playersWithMod.remove(player.getUuid())
        );

        AccurateDayNightCycle.CONFIG.subscribeToAccurateCelestialBodies(value -> {
            if (AccurateDayNightCycle.server != null)
                IpUtils.Geolocation.fromConfig().updatePlayersGeolocation(AccurateDayNightCycle.server);
        });
    }
}
