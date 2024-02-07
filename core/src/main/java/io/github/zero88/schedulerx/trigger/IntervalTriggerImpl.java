package io.github.zero88.schedulerx.trigger;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.impl.HumanReadableTimeFormat;
import io.github.zero88.schedulerx.trigger.rule.TriggerRule;

final class IntervalTriggerImpl implements IntervalTrigger {

    private final Duration initialDelay;
    private final Duration interval;
    private final long repeat;
    private final TriggerRule rule;

    IntervalTriggerImpl(Duration initialDelay, Duration interval, long repeat, TriggerRule rule) {
        this.repeat       = repeat;
        this.rule         = Optional.ofNullable(rule).orElse(TriggerRule.NOOP);
        this.interval     = Objects.requireNonNull(interval, "Interval configuration is required");
        this.initialDelay = Optional.ofNullable(this.rule.beginTime() == null ? initialDelay : Duration.ZERO)
                                    .orElse(Duration.ZERO);
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

    public @NotNull Instant nextTriggerTime(@NotNull Instant time) {
        validate();
        return time.plus(interval);
    }

    @Override
    public @NotNull List<OffsetDateTime> preview(@NotNull PreviewParameter parameter) {
        final PreviewParameter normalized = PreviewHelper.normalize(parameter, rule, ZoneOffset.UTC);
        if (repeat != REPEAT_INDEFINITELY) {
            normalized.setTimes((int) Math.min(repeat, parameter.getTimes()));
        }
        return PreviewHelper.preview(this, normalized.setStartedAt(normalized.getStartedAt().plus(initialDelay)));
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
