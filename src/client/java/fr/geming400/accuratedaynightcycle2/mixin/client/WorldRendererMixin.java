package fr.geming400.accuratedaynightcycle2.mixin.client;

import fr.geming400.accuratedaynightcycle2.AccurateDayNightCycle;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.shredzone.commons.suncalc.MoonPosition;
import org.shredzone.commons.suncalc.SunPosition;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.time.ZonedDateTime;

@Debug(export = true)
@Mixin(WorldRenderer.class)
abstract class WorldRendererMixin {
	@Shadow
	@Nullable
	private ClientWorld world;

	@Unique
	private float i;

	/**
	 * Normalizes the azimuth of suncalc into Minecraft's cardinal system
	 * @param azimuth the sun's azimuth
	 * @return the new azimuth
	 * @see SunPosition#getAzimuth()
	 */
	@Unique
	private static float normalizeAzimuth(double azimuth) {
		double newAzimuth = azimuth + 180; // For mc, 180° is north, and so 0° is south
		if (newAzimuth > 360)
			newAzimuth = newAzimuth - 360;

		return (float) newAzimuth;
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
		if (!AccurateDayNightCycle.CONFIG.useGeolocalisation()) return matrixStack;
		if (!AccurateDayNightCycle.CONFIG.accurateCelestialBodies()) return matrixStack;

		matrixStack.pop(); // Removing mc's sun matrix from the stack
		matrixStack.push(); // Pushing our own matrix instead

		assert this.world != null;

		ZonedDateTime zonedDateTime = AccurateDayNightCycle.getTime();
		SunPosition sunPosition = SunPosition.compute()
				.on(zonedDateTime)
				.timezone(zonedDateTime.getZone())
				.at(AccurateDayNightCycle.CONFIG.latitude(), AccurateDayNightCycle.CONFIG.longitude())
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

		if (i >= 360)
			i = 0;
		i += 0.2f;

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
		if (!AccurateDayNightCycle.CONFIG.accurateCelestialBodies()) return matrixStack;
		if (!AccurateDayNightCycle.CONFIG.useGeolocalisation()) return matrixStack;

		matrixStack.pop(); // Removing our sun matrix from the stack
		matrixStack.push(); // Pushing our new moon matrix

		ZonedDateTime zonedDateTime = AccurateDayNightCycle.getTime();
		MoonPosition moonPosition = MoonPosition.compute()
				.on(zonedDateTime)
				.timezone(zonedDateTime.getZone())
				.at(AccurateDayNightCycle.CONFIG.latitude(), AccurateDayNightCycle.CONFIG.longitude())
				.execute();

		// We put it on the horizon (north)
//		matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
		// matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));

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

		// We apply the moon pos
//		matrixStack.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(normalizeAzimuth((float) moonPosition.getAzimuth())));
//		matrixStack.multiply(RotationAxis.NEGATIVE_X.rotationDegrees((float) moonPosition.getAltitude()));

		// mc will pop our matrix stack eventually, so no need to call "MatrixStack#pop()"
		return matrixStack;
	}
}