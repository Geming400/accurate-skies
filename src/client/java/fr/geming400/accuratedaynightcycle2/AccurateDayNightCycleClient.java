package fr.geming400.accuratedaynightcycle2;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AccurateDayNightCycleClient implements ClientModInitializer {
	private static boolean warnAboutIris = false;

	public static void warnAboutIris() {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		if (player == null) return;
		if (warnAboutIris || AccurateDayNightCycle.CONFIG.accurateCelestialBodies()) {
			warnAboutIris = false;
			player.sendMessage(Text.translatable("acdn.warning.iris", Text.translatable("text.config.adnc-config.option.accurateCelestialBodies")).formatted(Formatting.GOLD));
		}
	}

	@Override
	public void onInitializeClient() {
		AccurateDayNightCycle.CONFIG.subscribeToAccurateCelestialBodies((value) -> {
			ClientPlayerEntity player = MinecraftClient.getInstance().player;
			if (FabricLoader.getInstance().isModLoaded("iris") && value == true) {
				warnAboutIris = true;
				if (player != null)
					warnAboutIris();
			}
		});
	}
}