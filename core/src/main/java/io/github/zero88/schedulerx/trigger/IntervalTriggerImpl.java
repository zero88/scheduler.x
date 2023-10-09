package io.github.zero88.schedulerx.trigger;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.impl.Utils;
import io.github.zero88.schedulerx.trigger.rule.TriggerRule;

final class IntervalTriggerImpl implements IntervalTrigger {

    private final TriggerRule rule;
    private final long repeat;
    private final long initialDelay;
    private final TimeUnit initialDelayTimeUnit;
    private final long interval;
    private final TimeUnit intervalTimeUnit;

    IntervalTriggerImpl(@NotNull TimeUnit initialDelayTimeUnit, long initialDelay, long repeat,
                        @NotNull TimeUnit intervalTimeUnit, long interval, TriggerRule rule) {
        this.rule                 = rule;
        this.initialDelayTimeUnit = initialDelayTimeUnit;
        this.initialDelay         = initialDelay;
        this.repeat               = repeat;
        this.intervalTimeUnit     = intervalTimeUnit;
        this.interval             = interval;
    }

    @Override
    public @NotNull TriggerRule rule() { return Optional.ofNullable(rule).orElseGet(IntervalTrigger.super::rule); }

    @Override
    public long getRepeat() { return repeat; }

    @Override
    public long getInitialDelay() { return initialDelay; }

    @Override
    public @NotNull TimeUnit getInitialDelayTimeUnit() { return this.initialDelayTimeUnit; }

    @Override
    public long getInterval() { return interval; }

    @Override
    public @NotNull TimeUnit getIntervalTimeUnit() { return this.intervalTimeUnit; }

    @Override
    @SuppressWarnings("java:S1192")
    public @NotNull IntervalTrigger validate() {
        validate(repeat, false, true, "repeat");
        validate(interval, false, false, "interval");
        validate(initialDelay, true, false, "initial delay");
        return this;
    }

    @Override
    public @NotNull List<OffsetDateTime> preview(@NotNull PreviewParameter parameter) {
        final List<OffsetDateTime> result = new ArrayList<>();
        final long count = Math.min(repeat, parameter.getTimes());
        final TriggerRule theRule = parameter.getRule();
        final ZoneId zoneId = Optional.ofNullable(parameter.getTimeZone()).orElse(ZoneOffset.UTC);
        Instant next = parameter.getStartedAt();
        next = next.plus(initialDelay, Utils.toChronoUnit(initialDelayTimeUnit));
        do {
            next = next.plus(interval, Utils.toChronoUnit(intervalTimeUnit));
            if (theRule.isExceeded(next)) {
                break;
            }
            if (theRule.satisfy(next)) {
                result.add(next.atZone(zoneId).toOffsetDateTime());
            }
        } while (result.size() != count);
        return result;
    }

    static void validate(long number, boolean allowZero, boolean allowInfinite, String msg) {
        if (number > 0 || (allowZero && number == 0) || (allowInfinite && number == REPEAT_INDEFINITELY)) {
            return;
        }
        throw new IllegalArgumentException("Invalid " + msg + " value");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        IntervalTriggerImpl that = (IntervalTriggerImpl) o;

        if (initialDelay != that.initialDelay) { return false; }
        if (repeat != that.repeat) { return false; }
        if (interval != that.interval) { return false; }
        if (initialDelayTimeUnit != that.initialDelayTimeUnit) { return false; }
        return intervalTimeUnit == that.intervalTimeUnit;
    }

    @Override
    public int hashCode() {
        int result = initialDelayTimeUnit.hashCode();
        result = 31 * result + (int) (initialDelay ^ (initialDelay >>> 32));
        result = 31 * result + (int) (repeat ^ (repeat >>> 32));
        result = 31 * result + intervalTimeUnit.hashCode();
        result = 31 * result + (int) (interval ^ (interval >>> 32));
        return result;
    }

    public String toString() {
        return "IntervalTrigger(initialDelay=" + initialDelay + ", initialDelayTimeUnit=" + initialDelayTimeUnit +
               ", interval=" + interval + ", intervalTimeUnit=" + intervalTimeUnit + ", repeat=" + repeat + ")";
    }

}
