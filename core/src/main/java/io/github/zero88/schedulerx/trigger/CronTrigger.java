package io.github.zero88.schedulerx.trigger;

import java.text.ParseException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.trigger.rule.TriggerRule;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Represents for inspecting settings specific to a CronTrigger.
 * <p/>
 * A cron trigger is a schedule that runs on specific different times that is defined with Unix 'cron-like' schedule
 * definitions.
 *
 * @since 1.0.0
 */
@JsonDeserialize(builder = CronTriggerBuilder.class)
public final class CronTrigger implements Trigger {

    public static final String TRIGGER_TYPE = "cron";

    public static CronTriggerBuilder builder() { return new CronTriggerBuilder(); }

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

    CronTrigger(@NotNull String expression, TimeZone timeZone) {
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
    public @NotNull String type() { return TRIGGER_TYPE; }

    @Override
    public @NotNull CronTrigger validate() {
        if (Objects.isNull(cronExpression)) {
            try {
                this.cronExpression = new CronExpression(expression).setTimeZone(timeZone);
            } catch (ParseException e) {
                throw new IllegalArgumentException("Cannot parse cron expression", e);
            }
        }
        return this;
    }

    @Override
    public @NotNull List<OffsetDateTime> preview(@NotNull PreviewParameter parameter) {
        validate();
        final List<OffsetDateTime> result = new ArrayList<>();
        final TriggerRule rule = parameter.getRule();
        final ZoneId zoneId = Optional.ofNullable(parameter.getTimeZone()).orElseGet(timeZone::toZoneId);
        Instant next = parameter.getStartedAt();
        do {
            next = cronExpression.getNextValidTimeAfter(Date.from(next)).toInstant();
            if (rule.isExceeded(next)) {
                break;
            }
            if (rule.satisfy(next)) {
                result.add(next.atZone(zoneId).toOffsetDateTime());
            }
        } while (result.size() != parameter.getTimes());
        return result;
    }

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
        return "CronTrigger(expression=" + expression + ", timeZone=" + timeZone.getID() + ')';
    }

}
