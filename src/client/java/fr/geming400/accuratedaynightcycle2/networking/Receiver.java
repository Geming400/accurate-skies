package fr.geming400.accuratedaynightcycle2.networking;

import fr.geming400.accuratedaynightcycle2.AccurateDayNightCycle;
import fr.geming400.accuratedaynightcycle2.AccurateDayNightCycleClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class Receiver {
    private Receiver() {}

    public static void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(GiveGeolocalisationS2CPayload.ID, (payload, context) -> {
            AccurateDayNightCycle.LOGGER.info("Received server's geolocalisation");

            AccurateDayNightCycleClient.geolocation = payload.data().geolocation();
            AccurateDayNightCycleClient.zoneID = payload.data().zoneID();
        });
    }
}
