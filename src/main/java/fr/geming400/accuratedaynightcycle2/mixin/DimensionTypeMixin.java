package fr.geming400.accuratedaynightcycle2.mixin;

import fr.geming400.accuratedaynightcycle2.moonphases.MoonPhases;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.ZonedDateTime;

@Mixin(DimensionType.class)
abstract class DimensionTypeMixin {
    // Yes we are applying this mixin even if we're not sure it's the overworld. But there's no way of knowing (I think)
    @Inject(at = @At("HEAD"), method = "getMoonPhase", cancellable = true)
    public void getMoonPhase(long time, CallbackInfoReturnable<Integer> cir) {
        // cir.setReturnValue(MoonPhase.Phase.NEW_MOON.ordinal());
        cir.setReturnValue(MoonPhases.getMoonPhase(ZonedDateTime.now()).ordinal());
    }
}
