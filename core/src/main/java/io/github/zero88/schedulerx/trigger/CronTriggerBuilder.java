package io.github.zero88.schedulerx.trigger;

import java.util.TimeZone;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.trigger.rule.TriggerRule;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Represents a builder that constructs {@link CronTrigger}
 */
@JsonPOJOBuilder(withPrefix = "")
@JsonIgnoreProperties(value = { "type" })
public final class CronTriggerBuilder {

    private TriggerRule rule;
    private String expression;
    private TimeZone timeZone;

    public CronTriggerBuilder rule(TriggerRule rule) {
        this.rule = rule;
        return this;
    }

    public CronTriggerBuilder expression(@NotNull String expression) {
        this.expression = expression;
        return this;
    }

    public CronTriggerBuilder timeZone(@NotNull TimeZone timeZone) {
        this.timeZone = timeZone;
        return this;
    }

    public CronTrigger build() { return new CronTriggerImpl(expression, timeZone, rule); }

}
