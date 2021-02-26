package io.github.zero88.vertx.scheduler.trigger;

import java.util.concurrent.TimeUnit;

import io.github.zero88.vertx.scheduler.Task;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;

/**
 * Represents for inspecting settings specific to an interval trigger, which is used to fire a <code>{@link Task}</code>
 * in periodic with repeat time.
 *
 * @since 1.0.0
 */
@Data
@Builder
@Jacksonized
public final class IntervalTrigger implements Trigger {

    /**
     * Used to indicate the 'repeat count' of the trigger is indefinite. Or in other words, the trigger should repeat
     * continually until the trigger's ending timestamp.
     */
    public static final long REPEAT_INDEFINITELY = -1;

    /**
     * Delay time unit
     *
     * @apiNote Default is {@code SECONDS}
     */
    @Default
    @NonNull
    private final TimeUnit initialDelayTimeUnit = TimeUnit.SECONDS;
    /**
     * Get the initial delay time (in {@link #getInitialDelayTimeUnit()}) before firing trigger in first time.
     *
     * @apiNote Default is {@code 0}
     */
    @Default
    private final long initialDelay = 0;
    /**
     * Get the number of times the {@code IntervalTrigger} should repeat, after which it will be automatically deleted.
     *
     * @see #REPEAT_INDEFINITELY
     */
    @Default
    private final long repeat = REPEAT_INDEFINITELY;
    /**
     * Interval time unit
     *
     * @apiNote Default is {@code SECONDS}
     */
    @Default
    @NonNull
    private final TimeUnit intervalTimeUnit = TimeUnit.SECONDS;
    /**
     * Get the time interval (in {@link #getIntervalTimeUnit()}) at which the {@code IntervalTrigger} should repeat.
     */
    private final long interval;

    static long validate(long number, boolean allowZero, boolean allowIndefinitely, String msg) {
        if (number > 0 || (allowZero && number == 0) || (allowIndefinitely && number == REPEAT_INDEFINITELY)) {
            return number;
        }
        throw new IllegalArgumentException("Invalid " + msg + " value");
    }

    public long getRepeat() {
        return validate(repeat, false, true, "repeat");
    }

    public long getInitialDelay() {
        return validate(initialDelay, true, false, "initial delay");
    }

    public long getInterval() {
        return validate(interval, false, false, "interval");
    }

    public long intervalInMilliseconds() {
        return TimeUnit.MILLISECONDS.convert(getInterval(), getIntervalTimeUnit());
    }

    public long delayInMilliseconds() {
        return TimeUnit.MILLISECONDS.convert(getInitialDelay(), getInitialDelayTimeUnit());
    }

    public boolean noDelay() {
        return getInitialDelay() == 0;
    }

    public boolean noRepeatIndefinitely() {
        return getRepeat() != REPEAT_INDEFINITELY;
    }

}
