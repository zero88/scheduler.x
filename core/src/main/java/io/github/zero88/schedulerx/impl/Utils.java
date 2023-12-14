package io.github.zero88.schedulerx.impl;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class Utils {

    public static String brackets(Object any) { return "[" + any + "]"; }

    /*
     * The random number generator, in a holder class to defer initialization until needed.
     */
    private static class Holder {

        static final SecureRandom numberGenerator = new SecureRandom();

    }

    private Utils() { }

    public static int randomPositiveInt() {
        return Utils.Holder.numberGenerator.nextInt() & Integer.MAX_VALUE;
    }

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

    @SuppressWarnings("unchecked")
    public static <T> T castOrNull(Object data, boolean nullOrThrow) {
        try {
            return (T) data;
        } catch (ClassCastException ex) {
            // @formatter:off
            if (nullOrThrow) return null;
            throw ex;
            // @formatter:on
        }
    }

    public static class HumanReadableTimeFormat {

        private HumanReadableTimeFormat() { }

        public static String format(Duration duration) {
            return duration.toString().substring(2).replaceAll("(\\d[HMS])(?!$)", "$1 ").toLowerCase();
        }

    }

}
