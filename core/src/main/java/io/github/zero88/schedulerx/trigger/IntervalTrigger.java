package io.github.zero88.schedulerx.trigger;

import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Represents for inspecting settings specific to a IntervalTrigger.
 * <p/>
 * An interval trigger is a schedule that repeats at a fixed interval of time starting from a given point in time, for
 * example, hourly, daily, every five minutes, every 30 seconds and so on.
 *
 * @since 1.0.0
 */
@JsonDeserialize(builder = IntervalTriggerBuilder.class)
public interface IntervalTrigger extends Trigger {

    String TRIGGER_TYPE = "interval";

    /**
     * Used to indicate the 'repeat count' of the trigger is indefinite. Or in other words, the trigger should repeat
     * continually until the trigger's ending timestamp.
     */
    long REPEAT_INDEFINITELY = -1;

    static IntervalTriggerBuilder builder() { return new IntervalTriggerBuilder(); }

    @Override
    default @NotNull String type() { return TRIGGER_TYPE; }

    /**
     * Get the number of times the {@code IntervalTrigger} should repeat, after which it will be automatically deleted.
     *
     * @see #REPEAT_INDEFINITELY
     */
    long getRepeat();

    /**
     * Get the initial delay time (in {@link #getInitialDelayTimeUnit()}) before emitting trigger in the first time.
     *
     * @apiNote Default is {@code 0}
     */
    long getInitialDelay();

    /**
     * Delay time unit
     *
     * @apiNote Default is {@code SECONDS}
     */
    @NotNull TimeUnit getInitialDelayTimeUnit();

    /**
     * Get the time interval (in {@link #getIntervalTimeUnit()}) at which the {@code IntervalTrigger} should repeat.
     */
    long getInterval();

    /**
     * Interval time unit
     *
     * @apiNote Default is {@code SECONDS}
     */
    @NotNull TimeUnit getIntervalTimeUnit();

    default boolean noDelay()              { return getInitialDelay() == 0; }

    default boolean noRepeatIndefinitely() { return getRepeat() != REPEAT_INDEFINITELY; }

    default long intervalInMilliseconds() {
        return TimeUnit.MILLISECONDS.convert(getInterval(), getIntervalTimeUnit());
    }

    default long delayInMilliseconds() {
        return TimeUnit.MILLISECONDS.convert(getInitialDelay(), getInitialDelayTimeUnit());
    }

    @Override
    @NotNull IntervalTrigger validate();

    @Override
    default JsonObject toJson() {
        JsonObject self = JsonObject.of("repeat", getRepeat(), "initialDelay", getInitialDelay(),
                                        "initialDelayTimeUnit", getInitialDelayTimeUnit(), "interval", getInterval(),
                                        "intervalTimeUnit", getIntervalTimeUnit());
        return Trigger.super.toJson().mergeIn(self);
    }

}
