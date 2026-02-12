package fr.geming400.accuratedaynightcycle2.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.function.Supplier;

public final class ModNetworking {
    private ModNetworking() {}

    public static void initialize() {
        PayloadTypeRegistry.playS2C().register(GiveGeolocalisationS2CPayload.ID, GiveGeolocalisationS2CPayload.CODEC);

        PayloadTypeRegistry.playC2S().register(ConfirmHasModC2SPayload.ID, ConfirmHasModC2SPayload.CODEC);
    }

    public static <B, V extends CustomPayload> PacketCodec<B, V> getEmptyPacketCodec(Supplier<V> recordConstructor) {
        return new PacketCodec<>() {
            @Override
            public V decode(B buf) {
                return recordConstructor.get();
            }

            @Override
            public void encode(B buf, V value) {}
        };
    }
}
