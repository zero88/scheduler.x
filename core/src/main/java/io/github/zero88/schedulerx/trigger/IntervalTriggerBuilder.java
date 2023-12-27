package io.github.zero88.schedulerx.trigger;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.zero88.schedulerx.impl.Utils;
import io.github.zero88.schedulerx.trigger.rule.TriggerRule;
import io.vertx.core.json.Json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a builder that constructs {@link IntervalTrigger}
 */
@Internal
@JsonIgnoreProperties(value = { "type" })
public final class IntervalTriggerBuilder {

    private Duration initialDelay;
    private TimeUnit initialDelayTimeUnit = TimeUnit.SECONDS;
    private long initialDelayLong = 0;
    private Duration interval;
    private TimeUnit intervalTimeUnit = TimeUnit.SECONDS;
    private long intervalLong = 0;
    private long repeat = IntervalTrigger.REPEAT_INDEFINITELY;
    private TriggerRule rule;

    IntervalTriggerBuilder() { }

    /**
     * @since 2.0.0
     */
    public IntervalTriggerBuilder initialDelay(Duration initialDelay) {
        this.initialDelay = initialDelay;
        return this;
    }

    /**
     * @since 2.0.0
     */
    public IntervalTriggerBuilder interval(Duration interval) {
        this.interval = interval;
        return this;
    }

    public IntervalTriggerBuilder repeat(long repeat) {
        this.repeat = repeat;
        return this;
    }

    public IntervalTriggerBuilder rule(TriggerRule rule) {
        this.rule = rule;
        return this;
    }

    /**
     * @deprecated Use {@link #initialDelay(Duration)}
     */
    public @Deprecated IntervalTriggerBuilder initialDelayTimeUnit(TimeUnit initialDelayTimeUnit) {
        if (initialDelayTimeUnit != null) { this.initialDelayTimeUnit = initialDelayTimeUnit; }
        return this;
    }

    /**
     * @deprecated Use {@link #initialDelay(Duration)}
     */
    public @Deprecated IntervalTriggerBuilder initialDelay(long initialDelay) {
        this.initialDelayLong = initialDelay;
        return this;
    }

    /**
     * @deprecated Use {@link #interval(Duration)}}
     */
    public @Deprecated IntervalTriggerBuilder intervalTimeUnit(TimeUnit intervalTimeUnit) {
        if (intervalTimeUnit != null) { this.intervalTimeUnit = intervalTimeUnit; }
        return this;
    }

    /**
     * @deprecated Use {@link #interval(Duration)}}
     */
    public @Deprecated IntervalTriggerBuilder interval(long interval) {
        this.intervalLong = interval;
        return this;
    }

    public IntervalTrigger build() {
        return new IntervalTriggerImpl(Optional.ofNullable(initialDelay)
                                               .orElseGet(() -> Duration.of(initialDelayLong,
                                                                            Utils.toChronoUnit(initialDelayTimeUnit))),
                                       Optional.ofNullable(interval)
                                               .orElseGet(() -> Duration.of(intervalLong,
                                                                            Utils.toChronoUnit(intervalTimeUnit))),
                                       repeat, rule);
    }

    static IntervalTrigger create(Map<String, Object> props) {
        Object initialDelayProp = props.get("initialDelay");
        Object intervalProp = props.get("interval");
        TriggerRule rule = Optional.ofNullable(props.get("rule"))
                                   .map(r -> Json.CODEC.fromValue(r, TriggerRule.class))
                                   .orElse(null);
        return new IntervalTriggerBuilder().initialDelay(parseDuration(initialDelayProp))
                                           .initialDelay(parseLong(initialDelayProp, 0))
                                           .initialDelayTimeUnit(parseTimeUnit(props.get("initialDelayTimeUnit")))
                                           .interval(parseDuration(intervalProp))
                                           .interval(parseLong(intervalProp, 0))
                                           .intervalTimeUnit(parseTimeUnit(props.get("intervalTimeUnit")))
                                           .repeat(parseLong(props.get("repeat"), IntervalTrigger.REPEAT_INDEFINITELY))
                                           .rule(rule)
                                           .build();
    }

    static Long parseLong(Object value, long defaultVal) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try { return Long.parseLong((String) value); } catch (NumberFormatException ignored) { /*ignored*/ }
        }
        return defaultVal;
    }

    static Duration parseDuration(Object value) {
        if (value instanceof Duration) {
            return (Duration) value;
        }
        if (value instanceof CharSequence) {
            try { return Duration.parse((CharSequence) value); } catch (DateTimeParseException ignored) {  /*ignored*/ }
        }
        return null;
    }

    static TimeUnit parseTimeUnit(Object value) {
        if (value instanceof TimeUnit) {
            return (TimeUnit) value;
        }
        if (value instanceof CharSequence) {
            return TimeUnit.valueOf(value.toString());
        }
        return null;
    }

}
