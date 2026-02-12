package fr.geming400.accuratedaynightcycle2;

import fr.geming400.accuratedaynightcycle2.networking.ClientReceiver;
import fr.geming400.accuratedaynightcycle2.utils.IpUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.ZoneId;

public class AccurateDayNightCycleClient implements ClientModInitializer {
	private static boolean warnAboutIris = false;
	private static boolean warnAboutSodium = false;

	@Nullable
	public static IpUtils.Geolocation geolocation = null;
	@Nullable
	public static ZoneId zoneID = null;

	public static void warnAboutIris() {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		if (player == null) return;
		if (
				warnAboutIris
				|| AccurateDayNightCycle.CONFIG.accurateCelestialBodies()
				&& FabricLoader.getInstance().isModLoaded("iris")
		) {
			warnAboutIris = false;
			player.sendMessage(Text.translatable("acdn.warning.iris", Text.translatable("text.config.adnc-config.option.accurateCelestialBodies")).formatted(Formatting.GOLD));
		}
	}
	public static void warnAboutSodium() {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		if (player == null) return;
		if (
				!AccurateDayNightCycle.CONFIG.shownSodiumWarning()
				&& AccurateDayNightCycle.CONFIG.accurateCelestialBodies()
				&& !FabricLoader.getInstance().isModLoaded("sodium")
				|| warnAboutSodium
		) {
			warnAboutSodium = false;
			AccurateDayNightCycle.CONFIG.shownSodiumWarning(true);
			player.sendMessage(Text.translatable("acdn.warning.sodium", Text.translatable("text.config.adnc-config.option.accurateCelestialBodies")).formatted(Formatting.RED));
		}
	}

	@Override
	public void onInitializeClient() {
		ClientReceiver.initialize();

		AccurateDayNightCycle.CONFIG.subscribeToAccurateCelestialBodies((value) -> {
			ClientPlayerEntity player = MinecraftClient.getInstance().player;
			if (FabricLoader.getInstance().isModLoaded("iris") && value == true) {
				warnAboutIris = true;
				if (player != null)
					warnAboutIris();
			} else if (!AccurateDayNightCycle.CONFIG.shownSodiumWarning() && !FabricLoader.getInstance().isModLoaded("sodium") && value == true) {
				warnAboutSodium = true;
				if (player != null)
					warnAboutSodium();
			}
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			if (geolocation != null || zoneID != null) {
				AccurateDayNightCycle.LOGGER.info("Resetting geo location data");

				geolocation = null;
				zoneID = null;
			}
		});
	}

	@NotNull
	public static IpUtils.Geolocation getGeolocation() {
		return geolocation == null ? IpUtils.Geolocation.fromConfig() : geolocation;
	}
	@NotNull
	public static ZoneId getZoneID() {
		return zoneID == null ? ZoneId.systemDefault() : zoneID;
	}
}