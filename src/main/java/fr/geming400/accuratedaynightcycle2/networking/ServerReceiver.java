package fr.geming400.accuratedaynightcycle2.networking;

import fr.geming400.accuratedaynightcycle2.AccurateDayNightCycle;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ServerReceiver {
    private ServerReceiver() {}

    public static void initialize() {
        ServerPlayNetworking.registerGlobalReceiver(ConfirmHasModC2SPayload.ID, (payload, context) ->
                AccurateDayNightCycle.playersWithMod.add(context.player().getUuid())
        );
    }
}
