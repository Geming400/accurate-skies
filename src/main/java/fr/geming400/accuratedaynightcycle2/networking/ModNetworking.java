package fr.geming400.accuratedaynightcycle2.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class ModNetworking {
    private ModNetworking() {}

    public static void initialize() {
        PayloadTypeRegistry.playS2C().register(GiveGeolocalisationS2CPayload.ID, GiveGeolocalisationS2CPayload.CODEC);
    }
}
