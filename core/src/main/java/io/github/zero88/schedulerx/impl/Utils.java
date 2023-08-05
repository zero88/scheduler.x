package io.github.zero88.schedulerx.impl;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public final class Utils {

    private Utils() { }

    /**
     * Converts this {@code TimeUnit} to the equivalent {@code ChronoUnit}.
     * <p>
     * Copy from java9
     *
     * @return the converted equivalent ChronoUnit
     * @since 2.0.0
     */
    public static ChronoUnit toChronoUnit(TimeUnit timeUnit) {
        // @formatter:off
        switch (timeUnit) {
            case NANOSECONDS:   return ChronoUnit.NANOS;
            case MICROSECONDS:  return ChronoUnit.MICROS;
            case MILLISECONDS:  return ChronoUnit.MILLIS;
            case SECONDS:       return ChronoUnit.SECONDS;
            case MINUTES:       return ChronoUnit.MINUTES;
            case HOURS:         return ChronoUnit.HOURS;
            case DAYS:          return ChronoUnit.DAYS;
            default:            throw new IllegalArgumentException("Unable to parse TimeUnit to ChronoUnit");
        }
        // @formatter:on
    }

}
