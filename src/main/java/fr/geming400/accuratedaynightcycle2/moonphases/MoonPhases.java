package fr.geming400.accuratedaynightcycle2.moonphases;

import fr.geming400.accuratedaynightcycle2.AccurateDayNightCycle;
import org.shredzone.commons.suncalc.MoonIllumination;
import org.shredzone.commons.suncalc.MoonPhase;

import java.time.ZonedDateTime;

public final class MoonPhases {
    private MoonPhases() {}

    public static final int MC_MOON_PHASE_LENGTH = 24000;

    public static MinecraftMoonPhases getMoonPhase(ZonedDateTime dateTime) {
        MoonIllumination moonIllumination = MoonIllumination.compute()
                .on(dateTime)
                .timezone(dateTime.getZone())
                .at(AccurateDayNightCycle.CONFIG.latitude(), AccurateDayNightCycle.CONFIG.longitude())
                .execute();

        // moonIllumination.getPhase() returns a value ∈ [-180, 180] and
        // MoonPhase.Phase.toPhase() uses an angle ∈ [0, 360]
        // So we add 180° to normalize it
        return MinecraftMoonPhases.fromSuncalcMoonPhase(
                MoonPhase.Phase.toPhase(moonIllumination.getPhase() + 180)
        );
    }

    public static long getMcMoonPhaseOffset(ZonedDateTime dateTime) {
        MinecraftMoonPhases minecraftMoonPhase = getMoonPhase(dateTime);

        int minecraftMoonPhaseFactor = minecraftMoonPhase != null ? minecraftMoonPhase.ordinal() : 0;
        return minecraftMoonPhaseFactor * MC_MOON_PHASE_LENGTH;
    }
}
