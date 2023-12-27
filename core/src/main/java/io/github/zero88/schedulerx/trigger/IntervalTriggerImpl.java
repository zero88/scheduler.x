package io.github.zero88.schedulerx.trigger;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.impl.Utils.HumanReadableTimeFormat;
import io.github.zero88.schedulerx.trigger.rule.TriggerRule;

final class IntervalTriggerImpl implements IntervalTrigger {

    private final Duration initialDelay;
    private final Duration interval;
    private final long repeat;
    private final TriggerRule rule;

    IntervalTriggerImpl(Duration initialDelay, Duration interval, long repeat, TriggerRule rule) {
        this.initialDelay = Optional.ofNullable(initialDelay).orElse(Duration.ZERO);
        this.interval     = Objects.requireNonNull(interval, "Interval configuration is required");
        this.repeat       = repeat;
        this.rule         = Optional.ofNullable(rule).orElseGet(IntervalTrigger.super::rule);
    }

    @Override
    public @NotNull TriggerRule rule() { return rule; }

    @Override
    public long getRepeat() { return repeat; }

    @Override
    public @NotNull Duration initialDelay() { return initialDelay; }

    @Override
    public @NotNull Duration interval() { return interval; }

    @Override
    @SuppressWarnings("java:S1192")
    public @NotNull IntervalTrigger validate() {
        if (repeat != REPEAT_INDEFINITELY && repeat <= 0) {
            throw new IllegalArgumentException("Invalid repeat value");
        }
        if (interval.isZero() || interval.isNegative()) {
            throw new IllegalArgumentException("Invalid interval value");
        }
        if (initialDelay.isNegative()) {
            throw new IllegalArgumentException("Invalid initial delay value");
        }
        return this;
    }

    @Override
    public @NotNull List<OffsetDateTime> preview(@NotNull PreviewParameter parameter) {
        final List<OffsetDateTime> result = new ArrayList<>();
        final long count = Math.min(repeat, parameter.getTimes());
        final TriggerRule theRule = parameter.getRule();
        final ZoneId zoneId = Optional.ofNullable(parameter.getTimeZone()).orElse(ZoneOffset.UTC);
        Instant next = parameter.getStartedAt();
        next = next.plus(initialDelay);
        do {
            next = next.plus(interval);
            if (theRule.isExceeded(next)) {
                break;
            }
            if (theRule.satisfy(next)) {
                result.add(next.atZone(zoneId).toOffsetDateTime());
            }
        } while (result.size() != count);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        IntervalTriggerImpl that = (IntervalTriggerImpl) o;

        if (repeat != that.repeat) { return false; }
        if (interval.compareTo(that.interval) != 0) { return false; }
        if (initialDelay.compareTo(that.initialDelay) != 0) { return false; }
        return rule.equals(that.rule);
    }

    @Override
    public int hashCode() {
        int result = (int) (repeat ^ (repeat >>> 32));
        result = 31 * result + initialDelay.hashCode();
        result = 31 * result + interval.hashCode();
        result = 31 * result + rule.hashCode();
        return result;
    }

    public String toString() {
        return "IntervalTrigger(initialDelay=" + HumanReadableTimeFormat.format(initialDelay) + ", interval=" +
               HumanReadableTimeFormat.format(interval) + ", repeat=" + repeat + ")";
    }

}
