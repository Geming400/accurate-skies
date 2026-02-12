package fr.geming400.accuratedaynightcycle2.mixin.client;

import fr.geming400.accuratedaynightcycle2.AccurateDayNightCycle;
import fr.geming400.accuratedaynightcycle2.AccurateDayNightCycleClient;
import fr.geming400.accuratedaynightcycle2.utils.IpUtils;
import fr.geming400.accuratedaynightcycle2.utils.Utils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.shredzone.commons.suncalc.MoonPosition;
import org.shredzone.commons.suncalc.SunPosition;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Debug(export = true)
@Mixin(WorldRenderer.class)
abstract class WorldRendererMixin {
	@Shadow
	@Nullable
	private ClientWorld world;

	@Unique
	private static final float MOON_TILT = 5.145f;
	/// Number made up by me, but it's here just to stylize the sun
	@Unique
	private static final float SUN_TILT = -2.35f;

	@Unique
	private static boolean isSodiumInstalled() {
		return FabricLoader.getInstance().isModLoaded("sodium");
	}

	/**
	 * Normalizes the altitude of suncalc into Minecraft's coordinate system
	 * @param sunPos the sun's position
	 * @return the new azimuth
	 * @see SunPosition#getAltitude()
	 */
	@Unique
	private static float normalizeAltitude(SunPosition sunPos) {
		// For the sun, 0° is the top of the sky, and so -90° is the horizon
		return (float) (sunPos.getAltitude() - 90);
	}
	/**
	 * Normalizes the altitude of suncalc into Minecraft's coordinate system.
	 * @param moonPos the moon's position
	 * @return the new azimuth
	 * @see MoonPosition#getAltitude()
	 */
	@Unique
	private static float normalizeAltitude(MoonPosition moonPos) {
		// For the moon, 0° is the bottom of the sky, and so 90° is the horizon
		return (float) (moonPos.getAltitude() + 90);
	}

	@ModifyVariable(
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lorg/joml/Quaternionf;)V",
					ordinal = 4,
					shift = At.Shift.AFTER
			),
			method = "renderSky"
	)
	private MatrixStack renderSun(MatrixStack matrixStack) {
		if (this.world != null && !Utils.checkDimension(this.world, Identifier.ofVanilla("overworld"))) return matrixStack;
		if (!AccurateDayNightCycle.CONFIG.useGeolocalisation()) return matrixStack;
		if (!AccurateDayNightCycle.CONFIG.accurateCelestialBodies()) return matrixStack;
		if (!isSodiumInstalled()) return matrixStack;

		matrixStack.pop(); // Removing mc's sun matrix from the stack
		matrixStack.push(); // Pushing our own matrix instead

		assert this.world != null;

		ZoneId zoneID = AccurateDayNightCycleClient.getZoneID();
		IpUtils.Geolocation geolocation = AccurateDayNightCycleClient.getGeolocation();
		SunPosition sunPosition = SunPosition.compute()
				.on(ZonedDateTime.now(zoneID))
				.timezone(zoneID)
				.at(geolocation.latitude(), geolocation.longitude())
				.execute();


		// We're using NEGATIVE_Y because getAzimuth() goes from north to east, while mc goes from north to west
		matrixStack.multiply(
				RotationAxis.NEGATIVE_Y.rotationDegrees(
						(float) sunPosition.getAzimuth()
				)
		);

		matrixStack.multiply(
				RotationAxis.POSITIVE_X.rotationDegrees(
						normalizeAltitude(sunPosition)
				)
		);

		matrixStack.multiply(
				RotationAxis.POSITIVE_Y.rotationDegrees(
						SUN_TILT
				)
		);

		// we will pop our matrix stack later (see next mixin)
		return matrixStack;
	}

	@ModifyVariable(
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/world/ClientWorld;getMoonPhase()I",
					shift = At.Shift.AFTER
			),
			method = "renderSky"
	)
	private MatrixStack renderMoon(MatrixStack matrixStack) {
		if (this.world != null && !Utils.checkDimension(this.world, Identifier.ofVanilla("overworld"))) return matrixStack;
		if (!AccurateDayNightCycle.CONFIG.accurateCelestialBodies()) return matrixStack;
		if (!AccurateDayNightCycle.CONFIG.useGeolocalisation()) return matrixStack;
		if (!isSodiumInstalled()) return matrixStack;

		matrixStack.pop(); // Removing our sun matrix from the stack
		matrixStack.push(); // Pushing our new moon matrix

		ZoneId zoneID = AccurateDayNightCycleClient.getZoneID();
		IpUtils.Geolocation geolocation = AccurateDayNightCycleClient.getGeolocation();
		MoonPosition moonPosition = MoonPosition.compute()
				.on(ZonedDateTime.now(zoneID))
				.timezone(zoneID)
				.at(geolocation.latitude(), geolocation.longitude())
				.execute();

		// We put it on the horizon (north)

		// We're using NEGATIVE_Y because getAzimuth() goes from north to east, while mc goes from north to west
		matrixStack.multiply(
				RotationAxis.NEGATIVE_Y.rotationDegrees(
						(float) moonPosition.getAzimuth()
				)
		);

		matrixStack.multiply(
				RotationAxis.POSITIVE_X.rotationDegrees(
						normalizeAltitude(moonPosition)
				)
		);

		matrixStack.multiply(
				RotationAxis.POSITIVE_Y.rotationDegrees(
						MOON_TILT
				)
		);

		// mc will pop our matrix stack eventually, so no need to call "MatrixStack#pop()"
		return matrixStack;
	}
}