package io.github.zero88.schedulerx.trigger;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Represents for inspecting settings specific to a IntervalTrigger.
 * <p/>
 * An interval trigger is a schedule that repeats at a fixed interval of time starting from a given point in time, for
 * example, hourly, daily, every five minutes, every 30 seconds and so on.
 *
 * @since 1.0.0
 */
public interface IntervalTrigger extends Trigger, TimebasedTrigger {

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
     * @return the initial delay time before emitting trigger in the first time.
     * @since 2.0.0
     */
    @NotNull Duration initialDelay();

    /**
     * @return the time interval at which the {@code IntervalTrigger} should repeat.
     * @since 2.0.0
     */
    @NotNull Duration interval();

    /**
     * Get the initial delay time (in {@link #getInitialDelayTimeUnit()}) before emitting trigger in the first time.
     *
     * @apiNote Default is {@code 0}
     * @deprecated use {@link #initialDelay()}
     */
    @Deprecated
    default long getInitialDelay() { return initialDelay().toSeconds(); }

    /**
     * Delay time unit
     *
     * @apiNote Default is {@code SECONDS}
     * @deprecated use {@link #initialDelay()}
     */
    @Deprecated
    default @NotNull TimeUnit getInitialDelayTimeUnit() { return TimeUnit.SECONDS; }

    /**
     * Get the time interval (in {@link #getIntervalTimeUnit()}) at which the {@code IntervalTrigger} should repeat.
     *
     * @deprecated use {@link #interval()}
     */
    @Deprecated
    default long getInterval() { return interval().toSeconds(); }

    /**
     * Interval time unit
     *
     * @apiNote Default is {@code SECONDS}
     * @deprecated use {@link #interval()}
     */
    @Deprecated
    default @NotNull TimeUnit getIntervalTimeUnit() { return TimeUnit.SECONDS; }

    @Override
    @NotNull IntervalTrigger validate();

    @Override
    default JsonObject toJson() {
        JsonObject self = JsonObject.of("repeat", getRepeat(), "initialDelay", initialDelay(), "interval", interval());
        return Trigger.super.toJson().mergeIn(self);
    }

    @JsonCreator
    static IntervalTrigger create(Map<String, Object> properties) {
        return IntervalTriggerBuilder.create(properties);
    }

}
