package fr.geming400.accuratedaynightcycle2.moonphases;

import org.jetbrains.annotations.Nullable;
import org.shredzone.commons.suncalc.MoonPhase;

public enum MinecraftMoonPhases {
    FULL_MOON,
    WANING_GIBBOUS,
    THIRD_QUARTER,
    WANING_CRESCENT,
    NEW_MOON,
    WAXING_CRESCENT,
    FIRST_QUARTER,
    WAXING_GIBBOUS;

    @Nullable
    public static MinecraftMoonPhases fromSuncalcMoonPhase(MoonPhase.Phase phase) {
        switch (phase) {
            case MoonPhase.Phase.FULL_MOON -> { return FULL_MOON; }
            case MoonPhase.Phase.WANING_GIBBOUS -> { return WANING_GIBBOUS; }
            case MoonPhase.Phase.LAST_QUARTER -> { return THIRD_QUARTER; }
            case MoonPhase.Phase.WANING_CRESCENT -> { return WANING_CRESCENT; }
            case MoonPhase.Phase.NEW_MOON -> { return NEW_MOON; }
            case MoonPhase.Phase.WAXING_CRESCENT -> { return WAXING_CRESCENT; }
            case MoonPhase.Phase.FIRST_QUARTER -> { return FIRST_QUARTER; }
            case MoonPhase.Phase.WAXING_GIBBOUS -> { return WAXING_GIBBOUS; }

            default -> { return null; }
        }
    }

    @Nullable
    public static MoonPhase.Phase fromMCMoonPhaseToSuncalcMoonPhase(MinecraftMoonPhases phase) {
        switch (phase) {
            case FULL_MOON -> { return MoonPhase.Phase.FULL_MOON; }
            case WANING_GIBBOUS -> { return MoonPhase.Phase.WANING_GIBBOUS; }
            case THIRD_QUARTER -> { return MoonPhase.Phase.LAST_QUARTER; }
            case WANING_CRESCENT -> { return MoonPhase.Phase.WANING_CRESCENT; }
            case NEW_MOON -> { return MoonPhase.Phase.NEW_MOON; }
            case WAXING_CRESCENT -> { return MoonPhase.Phase.WAXING_CRESCENT; }
            case FIRST_QUARTER -> { return MoonPhase.Phase.FIRST_QUARTER; }
            case WAXING_GIBBOUS -> { return MoonPhase.Phase.WAXING_GIBBOUS; }

            default -> { return null; }
        }
    }
}