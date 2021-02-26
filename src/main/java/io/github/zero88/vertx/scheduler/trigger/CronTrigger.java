package io.github.zero88.vertx.scheduler.trigger;

import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

import io.github.zero88.vertx.scheduler.Task;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder.Default;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * Represents for inspecting settings specific to a CronTrigger, which is used to fire a <code>{@link Task}</code> at
 * given moments in time, defined with Unix 'cron-like' schedule definitions.
 *
 * @since 1.0.0
 */
@Data
@SuperBuilder
@Jacksonized
public class CronTrigger implements Trigger {

    /**
     * Returns the cron expression
     *
     * @see CronExpression
     */
    @NonNull
    private final String expression;

    /**
     * Returns the time zone for which the {@code cronExpression} of this {@code CronTrigger} will be resolved.
     */
    @Default
    private final TimeZone timeZone = TimeZone.getTimeZone(ZoneOffset.UTC.getId());

    @JsonIgnore
    private CronExpression cronExpression;

    @JsonIgnore
    public CronExpression getCronExpression() {
        if (Objects.nonNull(cronExpression)) {
            return cronExpression;
        }
        try {
            return this.cronExpression = new CronExpression(expression).setTimeZone(timeZone);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Cannot parse cron expression", e);
        }
    }

    public long nextTriggerAfter(@NonNull Instant current) {
        final Instant next = getCronExpression().getNextValidTimeAfter(Date.from(current)).toInstant();
        return Math.max(1, ChronoUnit.MILLIS.between(current, next));
    }

}
