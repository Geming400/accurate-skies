package fr.geming400.accuratedaynightcycle2.mixin;

import fr.geming400.accuratedaynightcycle2.AccurateDayNightCycle;
import fr.geming400.accuratedaynightcycle2.moonphases.MoonPhases;
import fr.geming400.accuratedaynightcycle2.utils.Utils;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.shredzone.commons.suncalc.SunPosition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Mixin(World.class)
abstract class WorldMixin {
    @Shadow
    public abstract RegistryEntry<DimensionType> getDimensionEntry();

    @Unique
    private static final int TICKS_BEFORE_COMPUTING = 20;

    /// The number of iterations done. Goes from {@code [0, TICKS_BEFORE_COMPUTING[}
    /// @see #TICKS_BEFORE_COMPUTING
    @Unique
    private int iter = 0;

    /// The last registered time before another time got computed
    /// @see #iter
    @Unique
    private long lastTime = 0;

    @Inject(at = @At("HEAD"), method = "getTimeOfDay", cancellable = true)
    public void getTimeOfDay(CallbackInfoReturnable<Long> cir) {
        if (!Utils.checkDimension((World) (Object) this, Identifier.ofVanilla("overworld")))
            return;

        if (this.iter < TICKS_BEFORE_COMPUTING) {
            this.iter++;
            cir.setReturnValue(this.lastTime);
        }
        this.iter = 0;

        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        ZoneId zoneId = zonedDateTime.getZone();

        long mcMoonOffset = MoonPhases.getMcMoonPhaseOffset(zonedDateTime);
        if (AccurateDayNightCycle.CONFIG.useGeolocalisation()) {
            SunPosition sunPos = SunPosition.compute()
                    .on(zonedDateTime)
                    .timezone(zoneId)
                    .at(AccurateDayNightCycle.CONFIG.latitude(), AccurateDayNightCycle.CONFIG.longitude())
                    .execute();

            // 24000 is a minecraft day in ticks
            long mcTime = (long) ((sunPos.getAltitude() / 360) * 24000);

            this.lastTime = (mcTime + mcMoonOffset);
            cir.setReturnValue(this.lastTime);
        } else {
            // We apply the offset to get the "sun time"
            this.lastTime = Utils.timeToMcTime(zonedDateTime.minusSeconds(zonedDateTime.getOffset().getTotalSeconds()));

            cir.setReturnValue(this.lastTime + mcMoonOffset);
        }
    }
}
