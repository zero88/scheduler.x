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

final class CronTriggerImpl implements CronTrigger {

    private final TriggerRule rule;
    private final String expression;
    private final TimeZone timeZone;
    @JsonIgnore
    CronExpression cronExpression;

    CronTriggerImpl(@NotNull String expression, TimeZone timeZone, TriggerRule rule) {
        this.expression = Objects.requireNonNull(expression, "Cron expression is required");
        this.timeZone   = timeZone == null ? TimeZone.getTimeZone(ZoneOffset.UTC.getId()) : timeZone;
        this.rule       = rule;
    }

    @Override
    public @NotNull TriggerRule rule() { return Optional.ofNullable(rule).orElseGet(CronTrigger.super::rule); }

    public @NotNull String getExpression() { return this.expression; }

    public @NotNull TimeZone getTimeZone() { return this.timeZone; }

    public long nextTriggerAfter(@NotNull Instant time) {
        validate();
        final Instant next = cronExpression.getNextValidTimeAfter(Date.from(time)).toInstant();
        return Math.max(1, ChronoUnit.MILLIS.between(time, next));
    }

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
        final TriggerRule theRule = parameter.getRule();
        final ZoneId zoneId = Optional.ofNullable(parameter.getTimeZone()).orElseGet(timeZone::toZoneId);
        Instant next = parameter.getStartedAt();
        do {
            next = cronExpression.getNextValidTimeAfter(Date.from(next)).toInstant();
            if (theRule.isExceeded(next)) {
                break;
            }
            if (theRule.satisfy(next)) {
                result.add(next.atZone(zoneId).toOffsetDateTime());
            }
        } while (result.size() != parameter.getTimes());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        CronTriggerImpl that = (CronTriggerImpl) o;
        if (!expression.equals(that.expression)) { return false; }
        if (!timeZone.equals(that.timeZone)) { return false; }
        return rule().equals(that.rule());
    }

    @Override
    public int hashCode() {
        int result = expression.hashCode();
        result = 31 * result + timeZone.hashCode();
        result = 31 * result + rule().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "CronTrigger(expression=" + expression + ", timeZone=" + timeZone.getID() + ')';
    }

}
