package fr.geming400.accuratedaynightcycle2;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import fr.geming400.accuratedaynightcycle2.commands.ModCommands;
import fr.geming400.accuratedaynightcycle2.config.ModConfig;
import fr.geming400.accuratedaynightcycle2.networking.ModNetworking;
import fr.geming400.accuratedaynightcycle2.utils.IpUtils;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class AccurateDayNightCycle implements ModInitializer {
	public static final String MOD_ID = "accurate-day-night-cycle";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final ModConfig CONFIG = ModConfig.createAndLoad();

	public static final String IP_ADDRESS_UNSET = "DO NOT MODIFY - NOT SET";
	public static final String MAXMIND_DB_LINK = "https://github.com/P3TERX/GeoLite.mmdb/releases/latest/download/GeoLite2-City.mmdb";
	public static final File MAXMIND_DB_PATH = FabricLoader.getInstance().getConfigDir().resolve("GeoLite-city.mmdb").toFile();

	@Nullable
	private static MinecraftServer server = null;

	@Override
	public void onInitialize() {
		ModCommands.initialize();
		ModNetworking.initialize();

		ServerLifecycleEvents.SERVER_STARTED.register(mcServer -> {
			server = mcServer;
			loadOrCreateDb(false);
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(mcServer ->
				server = null
		);

		ServerPlayerEvents.JOIN.register(player -> {
			if (CONFIG.accurateCelestialBodies()) {
				LOGGER.info("Sending geolocation infos to player {}", player.getName().getString());
				IpUtils.Geolocation.fromConfig().updatePlayerGeolocation(player);
			}
		});

		if (!CONFIG.useGeolocalisation())
			CONFIG.accurateCelestialBodies(false);

		CONFIG.subscribeToUseGeolocalisation(value -> {
			if (!value) // If we disable geo localisation
				CONFIG.accurateCelestialBodies(false); // We also disable the 'accurateCelestialBodies' config
		});

		CONFIG.subscribeToAccurateCelestialBodies(value -> {
			if (server != null)
				IpUtils.Geolocation.fromConfig().updatePlayersGeolocation(server);

			if (!CONFIG.useGeolocalisation())
				CONFIG.accurateCelestialBodies(false);
		});

		if (CONFIG.ipAddress().equals(IP_ADDRESS_UNSET))
			loadOrCreateDb(false);
	}

	/// Checks if the {@linkplain fr.geming400.accuratedaynightcycle2.config.ModConfigModel#gmtTimeZone Mod Timezone} has been set to "auto" or not
	public static boolean isTimeZoneAuto() {
		return CONFIG.gmtTimeZone().equals("auto");
	}

	/// Gets the current time according to the {@link fr.geming400.accuratedaynightcycle2.config.ModConfigModel#gmtTimeZone} config
	/// @see #isTimeZoneAuto()
	public static ZonedDateTime getTime() {
		return isTimeZoneAuto()
				? ZonedDateTime.now()
				: ZonedDateTime.now(ZoneOffset.of(AccurateDayNightCycle.CONFIG.gmtTimeZone()));
	}

	/**
	 * Loads or create the MaxMind db. If it doesn't exist it will create it
	 * @return the {@link IpUtils.Geolocation} of the user
	 */
	@Nullable
	public static IpUtils.Geolocation loadOrCreateDb(boolean forceUpdate) {
		if (CONFIG.useGeolocalisation()) {
			LOGGER.info("Loading or creating MaxMind db (forceUpdate = {})", forceUpdate);

			try {
				InetAddress ip = IpUtils.getPublicIp();

				CONFIG.ipAddress(ip.toString());

				if (!Files.exists(MAXMIND_DB_PATH.toPath()) || forceUpdate) {
					if (forceUpdate && Files.exists(MAXMIND_DB_PATH.toPath())) {
						LOGGER.info("Deleting MaxMind db (at {}) because we are force updating it", MAXMIND_DB_PATH);
						Files.delete(MAXMIND_DB_PATH.toPath());
					}

					Files.createDirectories(MAXMIND_DB_PATH.toPath().getParent());
					try (
							HttpClient client = HttpClient.newBuilder()
									.followRedirects(HttpClient.Redirect.NORMAL)
									.build()
					) {
						HttpRequest request = HttpRequest.newBuilder()
								.uri(URI.create(MAXMIND_DB_LINK))
								.GET()
								.build();

						LOGGER.info("Downloading MaxMind db");
						HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
						if (response.statusCode() >= 200 && response.statusCode() < 300)
							LOGGER.warn("Got status code {} when trying to download MaxMind DB. Continuing but errors might appear", response.statusCode());

						LOGGER.info("Writing MaxMind db file to {}", MAXMIND_DB_PATH);
						Files.createFile(MAXMIND_DB_PATH.toPath());
						Files.write(MAXMIND_DB_PATH.toPath(), response.body(), StandardOpenOption.WRITE);

						IpUtils.Geolocation geolocation = IpUtils.getCoordinatesFromIp(ip, MAXMIND_DB_PATH);

						// Setting the new latitude and longitude in the config
						CONFIG.latitude(geolocation.latitude());
						CONFIG.longitude(geolocation.longitude());

						return geolocation;
					} catch (IOException | InterruptedException e) {
						AccurateDayNightCycle.LOGGER.error("Tried getting the MaxMind db but failed", e);
					}
				} else {
					LOGGER.info("Db was already created, not recreating it again");
					return new IpUtils.Geolocation(CONFIG.latitude(), CONFIG.longitude());
				}
			} catch (IOException | GeoIp2Exception e) {
				throw new RuntimeException(e);
			}
		}

        return null;
    }
}