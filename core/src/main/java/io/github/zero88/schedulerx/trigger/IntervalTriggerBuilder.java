package io.github.zero88.schedulerx.trigger;

import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Represents a builder that constructs {@link IntervalTrigger}
 */
@JsonPOJOBuilder(withPrefix = "")
public final class IntervalTriggerBuilder {

    private TimeUnit initialDelayTimeUnit = TimeUnit.SECONDS;
    private long initialDelay = 0;
    private long repeat = IntervalTrigger.REPEAT_INDEFINITELY;
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
        return new IntervalTrigger(initialDelayTimeUnit, initialDelay, repeat, intervalTimeUnit, interval);
    }

}
