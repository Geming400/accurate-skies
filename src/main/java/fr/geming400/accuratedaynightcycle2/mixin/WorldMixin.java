package fr.geming400.accuratedaynightcycle2.mixin;

import fr.geming400.accuratedaynightcycle2.AccurateDayNightCycle;
import fr.geming400.accuratedaynightcycle2.moonphases.MoonPhases;
import net.minecraft.world.World;
import org.shredzone.commons.suncalc.SunTimes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Mixin(World.class)
abstract class WorldMixin {
    @Unique
    private static final int TICKS_BEFORE_COMPUTER = 20;

    /// The number of iterations done. Goes from [0, TICKS_BEFORE_COMPUTER[
    /// @see #TICKS_BEFORE_COMPUTER
    @Unique
    private int iter = 0;

    /// The last registered time before another time got computed
    /// @see #iter
    @Unique
    private long lastTime = 0;

    @Unique
    private long timeToMcTime(long time) {
        // This is the simplified formula
        // Original one that I found:
        //
        // 86400 = 43200*2 ticks
        // 24000 - ((time / 86400) * 24000 + 6000
        return -(5 * time)/18 + 18000;
    }
    @Unique
    private long timeToMcTime(LocalTime time) {
        return this.timeToMcTime(time.toSecondOfDay());
    }
    @Unique
    private long timeToMcTime(ZonedDateTime time) {
        return this.timeToMcTime(time.toLocalTime());
    }

    @Inject(at = @At("HEAD"), method = "getTimeOfDay", cancellable = true)
    public void getTimeOfDay(CallbackInfoReturnable<Long> cir) {
        if (this.iter < TICKS_BEFORE_COMPUTER) {
            this.iter++;
            cir.setReturnValue(this.lastTime);
        }
        this.iter = 0;

        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        ZoneId zoneId = zonedDateTime.getZone();

        long mcMoonOffset = MoonPhases.getMcMoonPhaseOffset(zonedDateTime);
        if (AccurateDayNightCycle.CONFIG.useGeolocalisation()) {
            SunTimes sunTimes = SunTimes.compute()
                    .on(zonedDateTime)
                    .timezone(zoneId)
                    .at(AccurateDayNightCycle.CONFIG.latitude(), AccurateDayNightCycle.CONFIG.longitude())
                    .execute();

            long mcTime = this.lastTime;

            if (sunTimes.getNoon() != null) {
                // 43200 = 24H/2
                double timeFactor = (double) zonedDateTime.toLocalTime().toSecondOfDay() / sunTimes.getNoon().toLocalTime().toSecondOfDay();
                mcTime = this.timeToMcTime((long) (timeFactor * 43200L));
            }

            this.lastTime = (mcTime + mcMoonOffset);
            cir.setReturnValue(this.lastTime);
        } else {
            // We apply the offset to get the "sun time"
            this.lastTime = this.timeToMcTime(zonedDateTime.minusSeconds(zonedDateTime.getOffset().getTotalSeconds()));
            cir.setReturnValue(this.lastTime + mcMoonOffset);
        }
    }
}
