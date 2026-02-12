package fr.geming400.accuratedaynightcycle2.mixin.client;

import fr.geming400.accuratedaynightcycle2.AccurateDayNightCycleClient;
import fr.geming400.accuratedaynightcycle2.networking.ConfirmHasModC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
abstract class ClientPlayNetworkHandlerMixin {
    @Inject(at = @At("TAIL"), method = "onGameJoin")
    private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        AccurateDayNightCycleClient.warnAboutSodium();
        AccurateDayNightCycleClient.warnAboutIris();

        ClientPlayNetworking.send(new ConfirmHasModC2SPayload());
    }
}
