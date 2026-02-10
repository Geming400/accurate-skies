package fr.geming400.accuratedaynightcycle2.mixin.client;

import fr.geming400.accuratedaynightcycle2.AccurateDayNightCycle;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
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

	/**
	 * Normalizes the azimuth of suncalc into Minecraft's cardinal system
	 * @param azimuth the sun's azimuth
	 * @return the new azimuth
	 * @see SunPosition#getAzimuth()
	 */
	@Unique
	private static float normalizeAzimuth(double azimuth) {
		double newAzimuth = azimuth + 180; // For mc, 180° is north
		if (newAzimuth > 360)
			newAzimuth = newAzimuth - 360;

		return (float) newAzimuth;
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

		matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(normalizeAzimuth(sunPosition.getAzimuth())));
		matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) sunPosition.getAltitude()));

		// mc will pop our matrix stack eventually, so no need to call "MatrixStack#pop()"
		return matrixStack;
	}
}