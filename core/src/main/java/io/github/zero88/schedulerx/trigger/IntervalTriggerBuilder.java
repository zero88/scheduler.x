package io.github.zero88.schedulerx.trigger;

import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.trigger.rule.TriggerRule;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Represents a builder that constructs {@link IntervalTrigger}
 */
@JsonPOJOBuilder(withPrefix = "")
@JsonIgnoreProperties(value = { "type" })
public final class IntervalTriggerBuilder {

    private TimeUnit initialDelayTimeUnit = TimeUnit.SECONDS;
    private long initialDelay = 0;
    private long repeat = IntervalTrigger.REPEAT_INDEFINITELY;
    private TimeUnit intervalTimeUnit = TimeUnit.SECONDS;
    private long interval;
    private TriggerRule rule;

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

    public IntervalTriggerBuilder rule(TriggerRule rule) {
        this.rule = rule;
        return this;
    }

    public IntervalTrigger build() {
        return new IntervalTriggerImpl(initialDelayTimeUnit, initialDelay, repeat, intervalTimeUnit, interval, rule);
    }

}
