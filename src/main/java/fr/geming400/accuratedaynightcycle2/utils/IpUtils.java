package fr.geming400.accuratedaynightcycle2.utils;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import fr.geming400.accuratedaynightcycle2.AccurateDayNightCycle;
import fr.geming400.accuratedaynightcycle2.networking.GiveGeolocalisationS2CPayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class IpUtils {
    private static Optional<String> fetchIp() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.ipify.org"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return Optional.of(response.body());
        } catch (IOException | InterruptedException e) {
            AccurateDayNightCycle.LOGGER.error("Tried fetching user's ip but failed", e);
            return Optional.empty();
        }
    }

    public static InetAddress getPublicIp() throws IOException {
        return InetAddress.getByName(fetchIp().orElseThrow());
    }

    public static String getLocalIp() throws UnknownHostException {
        InetAddress localIP = InetAddress.getLocalHost();
        return localIP.getHostAddress();
    }

    public static IpUtils.Geolocation getCoordinatesFromIp(InetAddress ip, File db) throws IOException, GeoIp2Exception {
        try (DatabaseReader reader = new DatabaseReader.Builder(db).build()) {
            CityResponse response = reader.city(ip);
            return IpUtils.Geolocation.of(response);
        }
    }

    public record Geolocation(double latitude, double longitude) {
        public static final PacketCodec<PacketByteBuf, Geolocation> PACKET_CODEC = PacketCodec.tuple(
                PacketCodecs.DOUBLE, Geolocation::latitude,
                PacketCodecs.DOUBLE, Geolocation::longitude,
                Geolocation::new
        );

        public void updatePlayerGeolocation(ServerPlayerEntity player) {
            ServerPlayNetworking.send(player, new GiveGeolocalisationS2CPayload(
                    new GiveGeolocalisationS2CPayload.Data(this, AccurateDayNightCycle.getTime().getZone()))
            );
        }

        public void updatePlayersGeolocation(MinecraftServer server) {
            for (ServerPlayerEntity player : PlayerLookup.all(server)) {
                this.updatePlayerGeolocation(player);
            }
        }

        public static Geolocation of(CityResponse cityResponse) {
            return new Geolocation(
                    cityResponse.getLocation().getLatitude(),
                    cityResponse.getLocation().getLongitude()
            );
        }

        public static Geolocation fromConfig() {
            return new Geolocation(
                    AccurateDayNightCycle.CONFIG.latitude(),
                    AccurateDayNightCycle.CONFIG.longitude()
            );
        }
    }
}
