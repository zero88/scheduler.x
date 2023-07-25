package io.github.zero88.schedulerx.trigger;

import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.Task;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Represents for inspecting settings specific to an interval trigger, which is used to fire a <code>{@link Task}</code>
 * in periodic with repeat time.
 *
 * @since 1.0.0
 */
@JsonDeserialize(builder = IntervalTrigger.IntervalTriggerBuilder.class)
public final class IntervalTrigger implements Trigger {

    /**
     * Used to indicate the 'repeat count' of the trigger is indefinite. Or in other words, the trigger should repeat
     * continually until the trigger's ending timestamp.
     */
    public static final long REPEAT_INDEFINITELY = -1;
    /**
     * Get the number of times the {@code IntervalTrigger} should repeat, after which it will be automatically deleted.
     *
     * @see #REPEAT_INDEFINITELY
     */
    private final long repeat;
    /**
     * Get the initial delay time (in {@link #getInitialDelayTimeUnit()}) before firing trigger in first time.
     *
     * @apiNote Default is {@code 0}
     */
    private final long initialDelay;
    /**
     * Delay time unit
     *
     * @apiNote Default is {@code SECONDS}
     */
    @NotNull
    private final TimeUnit initialDelayTimeUnit;
    /**
     * Get the time interval (in {@link #getIntervalTimeUnit()}) at which the {@code IntervalTrigger} should repeat.
     */
    private final long interval;
    /**
     * Interval time unit
     *
     * @apiNote Default is {@code SECONDS}
     */
    @NotNull
    private final TimeUnit intervalTimeUnit;

    IntervalTrigger(@NotNull TimeUnit initialDelayTimeUnit, long initialDelay, long repeat,
                    @NotNull TimeUnit intervalTimeUnit, long interval) {
        this.initialDelayTimeUnit = initialDelayTimeUnit;
        this.initialDelay         = initialDelay;
        this.repeat               = repeat;
        this.intervalTimeUnit     = intervalTimeUnit;
        this.interval             = interval;
    }

    public long getRepeat()                            { return repeat; }

    public long getInitialDelay()                      { return initialDelay; }

    public @NotNull TimeUnit getInitialDelayTimeUnit() { return this.initialDelayTimeUnit; }

    public long getInterval()                          { return interval; }

    public @NotNull TimeUnit getIntervalTimeUnit()     { return this.intervalTimeUnit; }

    public long intervalInMilliseconds() {
        return TimeUnit.MILLISECONDS.convert(interval, intervalTimeUnit);
    }

    public long delayInMilliseconds() {
        return TimeUnit.MILLISECONDS.convert(initialDelay, initialDelayTimeUnit);
    }

    public boolean noDelay() {
        return initialDelay == 0;
    }

    public boolean noRepeatIndefinitely() {
        return repeat != REPEAT_INDEFINITELY;
    }

    public static IntervalTriggerBuilder builder() { return new IntervalTriggerBuilder(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        IntervalTrigger that = (IntervalTrigger) o;

        if (initialDelay != that.initialDelay) { return false; }
        if (repeat != that.repeat) { return false; }
        if (interval != that.interval) { return false; }
        if (initialDelayTimeUnit != that.initialDelayTimeUnit) { return false; }
        return intervalTimeUnit == that.intervalTimeUnit;
    }

    @Override
    public int hashCode() {
        int result = initialDelayTimeUnit.hashCode();
        result = 31 * result + (int) (initialDelay ^ (initialDelay >>> 32));
        result = 31 * result + (int) (repeat ^ (repeat >>> 32));
        result = 31 * result + intervalTimeUnit.hashCode();
        result = 31 * result + (int) (interval ^ (interval >>> 32));
        return result;
    }

    public String toString() {
        return "IntervalTrigger(initialDelayTimeUnit=" + initialDelayTimeUnit + ", initialDelay=" + initialDelay +
               ", repeat=" + repeat + ", intervalTimeUnit=" + intervalTimeUnit + ", interval=" + interval + ")";
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class IntervalTriggerBuilder {

        private TimeUnit initialDelayTimeUnit = TimeUnit.SECONDS;
        private long initialDelay = 0;
        private long repeat = REPEAT_INDEFINITELY;
        private TimeUnit intervalTimeUnit = TimeUnit.SECONDS;
        private long interval;

        public IntervalTriggerBuilder initialDelayTimeUnit(@NotNull TimeUnit initialDelayTimeUnit) {
            this.initialDelayTimeUnit = initialDelayTimeUnit;
            return this;
        }

        public IntervalTriggerBuilder initialDelay(long initialDelay) {
            this.initialDelay = initialDelay;
            return this;
        }

        public IntervalTriggerBuilder repeat(long repeat) {
            this.repeat = repeat;
            return this;
        }

        public IntervalTriggerBuilder intervalTimeUnit(@NotNull TimeUnit intervalTimeUnit) {
            this.intervalTimeUnit = intervalTimeUnit;
            return this;
        }

        public IntervalTriggerBuilder interval(long interval) {
            this.interval = interval;
            return this;
        }

        public IntervalTrigger build() {
            return new IntervalTrigger(initialDelayTimeUnit, validate(initialDelay, true, false, "initial delay"),
                                       validate(repeat, false, true, "repeat"), intervalTimeUnit,
                                       validate(interval, false, false, "interval"));
        }

        static long validate(long number, boolean allowZero, boolean allowIndefinitely, String msg) {
            if (number > 0 || (allowZero && number == 0) || (allowIndefinitely && number == REPEAT_INDEFINITELY)) {
                return number;
            }
            throw new IllegalArgumentException("Invalid " + msg + " value");
        }

    }

}
