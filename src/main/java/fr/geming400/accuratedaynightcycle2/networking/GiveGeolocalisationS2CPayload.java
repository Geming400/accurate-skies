package fr.geming400.accuratedaynightcycle2.networking;

import fr.geming400.accuratedaynightcycle2.AccurateDayNightCycle;
import fr.geming400.accuratedaynightcycle2.utils.IpUtils;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.time.ZoneId;

public record GiveGeolocalisationS2CPayload(Data data) implements CustomPayload {
    public static final Identifier GIVE_GEOLOCALISATION_PAYLOAD_ID = Identifier.of(AccurateDayNightCycle.MOD_ID, "give_geolocalisation");
    public static final CustomPayload.Id<GiveGeolocalisationS2CPayload> ID = new CustomPayload.Id<>(GIVE_GEOLOCALISATION_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, GiveGeolocalisationS2CPayload> CODEC = PacketCodec.tuple(
            Data.PACKET_CODEC, GiveGeolocalisationS2CPayload::data,
            GiveGeolocalisationS2CPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public record Data(IpUtils.Geolocation geolocation, ZoneId zoneID) {
        public static final PacketCodec<PacketByteBuf, Data> PACKET_CODEC = new PacketCodec<>() {
            @Override
            public Data decode(PacketByteBuf buf) {
                IpUtils.Geolocation geolocation = IpUtils.Geolocation.PACKET_CODEC.decode(buf);
                ZoneId zoneID = ZoneId.of(PacketCodecs.STRING.decode(buf));

                return new Data(geolocation, zoneID);
            }

            @Override
            public void encode(PacketByteBuf buf, Data value) {
                IpUtils.Geolocation.PACKET_CODEC.encode(buf, value.geolocation);
                PacketCodecs.STRING.encode(buf, value.zoneID.getId());
            }
        };
    }
}
