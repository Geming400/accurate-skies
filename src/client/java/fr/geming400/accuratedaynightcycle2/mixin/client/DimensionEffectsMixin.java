package fr.geming400.accuratedaynightcycle2.mixin.client;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import fr.geming400.accuratedaynightcycle2.AccurateDayNightCycle;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.DimensionEffects;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Debug(export = true)
@Mixin(DimensionEffects.class)
abstract class DimensionEffectsMixin {
    /**
     * Used to make the sunset appear later in the sky.
     * Why ?
     * Well if we don't do that, the "illusion" of the sunset being oriented
     * as the same way the sun is (with our new sun position applied !)
     * the sunset will appear earlier than it should.
     * <p>
     * Hence the role of this mixin.
     */
    @Definition(id = "g", local = @Local(type = float.class, ordinal = 3))
    @Expression("g >= -0.4")
    @ModifyExpressionValue(method = "getFogColorOverride", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean yourHandler(boolean original, @Local(ordinal = 3) float g) {
        if (!AccurateDayNightCycle.CONFIG.useGeolocalisation()) return false;
        if (!AccurateDayNightCycle.CONFIG.accurateCelestialBodies()) return false;
        if (!FabricLoader.getInstance().isModLoaded("sodium")) return false;

        return g >= -0.28;
    }
}
