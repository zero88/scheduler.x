package io.github.zero88.schedulerx.trigger;

import java.util.TimeZone;

import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Represents a builder that constructs {@link CronTrigger}
 */
@JsonPOJOBuilder(withPrefix = "")
public final class CronTriggerBuilder {

    private String expression;
    private TimeZone timeZone;

    public CronTriggerBuilder expression(@NotNull String expression) {
        this.expression = expression;
        return this;
    }

    public CronTriggerBuilder timeZone(@NotNull TimeZone timeZone) {
        this.timeZone = timeZone;
        return this;
    }

    public CronTrigger build() { return new CronTrigger(this.expression, this.timeZone); }

}
