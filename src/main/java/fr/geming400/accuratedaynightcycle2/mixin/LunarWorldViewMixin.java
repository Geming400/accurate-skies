package fr.geming400.accuratedaynightcycle2.mixin;

import fr.geming400.accuratedaynightcycle2.AccurateDayNightCycle;
import fr.geming400.accuratedaynightcycle2.moonphases.MoonPhases;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionType.class)
abstract class LunarWorldViewMixin {
    @Inject(at = @At("HEAD"), method = "getMoonPhase", cancellable = true)
    public void getMoonPhase(long time, CallbackInfoReturnable<Integer> cir) {
        // cir.setReturnValue(MoonPhase.Phase.NEW_MOON.ordinal());
        cir.setReturnValue(MoonPhases.getMoonPhase(AccurateDayNightCycle.getTime()).ordinal());
    }
}
