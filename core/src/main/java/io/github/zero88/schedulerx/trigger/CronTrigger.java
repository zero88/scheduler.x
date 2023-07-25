package io.github.zero88.schedulerx.trigger;

import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.Task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Represents for inspecting settings specific to a CronTrigger, which is used to fire a <code>{@link Task}</code> at
 * given moments in time, defined with Unix 'cron-like' schedule definitions.
 *
 * @since 1.0.0
 */
@JsonDeserialize(builder = CronTrigger.CronTriggerBuilder.class)
public final class CronTrigger implements Trigger {

    /**
     * Returns the cron expression
     *
     * @see CronExpression
     */
    @NotNull
    private final String expression;

    /**
     * Returns the time zone for which the {@code cronExpression} of this {@code CronTrigger} will be resolved.
     */
    @NotNull
    private final TimeZone timeZone;

    @JsonIgnore
    private CronExpression cronExpression;

    private CronTrigger(@NotNull String expression, TimeZone timeZone) {
        this.expression = Objects.requireNonNull(expression, "Cron expression is required");
        this.timeZone   = timeZone == null ? TimeZone.getTimeZone(ZoneOffset.UTC.getId()) : timeZone;
    }

    public @NotNull String getExpression() { return this.expression; }

    public @NotNull TimeZone getTimeZone() { return this.timeZone; }

    @JsonIgnore
    public CronExpression getCronExpression() { return cronExpression; }

    public long nextTriggerAfter(@NotNull Instant current) {
        final Instant next = validate().cronExpression.getNextValidTimeAfter(Date.from(current)).toInstant();
        return Math.max(1, ChronoUnit.MILLIS.between(current, next));
    }

    @Override
    public CronTrigger validate() {
        if (Objects.isNull(cronExpression)) {
            try {
                this.cronExpression = new CronExpression(expression).setTimeZone(timeZone);
            } catch (ParseException e) {
                throw new IllegalArgumentException("Cannot parse cron expression", e);
            }
        }
        return this;
    }

    public static CronTriggerBuilder builder() { return new CronTriggerBuilder(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        CronTrigger that = (CronTrigger) o;
        return expression.equals(that.expression) && timeZone.equals(that.timeZone);
    }

    @Override
    public int hashCode() {
        int result = expression.hashCode();
        result = 31 * result + timeZone.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "CronTrigger(expression=" + expression + ", timeZone=" + timeZone + ')';
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class CronTriggerBuilder {

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

}
