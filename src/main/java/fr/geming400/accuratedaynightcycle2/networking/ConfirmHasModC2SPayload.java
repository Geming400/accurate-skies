package fr.geming400.accuratedaynightcycle2.networking;

import fr.geming400.accuratedaynightcycle2.AccurateDayNightCycle;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ConfirmHasModC2SPayload() implements CustomPayload {
    public static final Identifier CONFIRM_HAS_MOD_PAYLOAD_ID = Identifier.of(AccurateDayNightCycle.MOD_ID, "confirm_has_mod");
    public static final CustomPayload.Id<ConfirmHasModC2SPayload> ID = new CustomPayload.Id<>(CONFIRM_HAS_MOD_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, ConfirmHasModC2SPayload> CODEC = ModNetworking.getEmptyPacketCodec(ConfirmHasModC2SPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}