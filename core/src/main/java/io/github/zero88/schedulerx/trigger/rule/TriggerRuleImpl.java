package io.github.zero88.schedulerx.trigger.rule;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.DefaultOptions;

@SuppressWarnings("rawtypes")
final class TriggerRuleImpl implements TriggerRule {

    private final List<Timeframe> timeframes;
    private final Instant beginTime;
    private final Instant until;
    private final Duration leeway;
    private final int hashCode;

    TriggerRuleImpl(Instant beginTime, Instant until, List<Timeframe> timeframes, Duration leeway) {
        validateBeginUntil(beginTime, until);
        this.beginTime  = beginTime;
        this.until      = until;
        this.timeframes = Optional.ofNullable(timeframes).orElseGet(Collections::emptyList);
        this.leeway     = validateLeewayTime(leeway);
        this.hashCode   = computeHashCode();
    }

    @Override
    public @NotNull List<Timeframe> timeframes() { return timeframes; }

    @Override
    public Instant beginTime() { return beginTime; }

    @Override
    public Instant until() { return until; }

    @Override
    public @NotNull Duration leeway() { return leeway; }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        TriggerRuleImpl that = (TriggerRuleImpl) o;
        return Objects.equals(beginTime, that.beginTime) && Objects.equals(until, that.until) &&
               Objects.equals(leeway, that.leeway) && timeframes.equals(that.timeframes);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "TriggerRule{begin=" + beginTime + ",until=" + until + ", leeway=" + leeway + ", " + timeframes + "}";
    }

    private int computeHashCode() {
        int result = Optional.ofNullable(beginTime).map(Instant::hashCode).orElse(0);
        result = 31 * result + Optional.ofNullable(until).map(Instant::hashCode).orElse(0);
        result = 31 * result + leeway.hashCode();
        result = 31 * result + timeframes.hashCode();
        return result;
    }

    @NotNull
    private static Duration validateLeewayTime(Duration leeway) {
        final Duration given = Optional.ofNullable(leeway).orElse(Duration.ZERO);
        if (given.isNegative()) {
            return Duration.ZERO;
        }
        final Duration maxLeeway = DefaultOptions.getInstance().triggerRuleMaxLeeway;
        return given.compareTo(maxLeeway) > 0 ? maxLeeway : given;
    }

    private static void validateBeginUntil(Instant beginTime, Instant until) {
        if (beginTime == null || until == null) {
            return;
        }
        if (beginTime.compareTo(until) >= 0) {
            throw new IllegalArgumentException("The 'begin time' must be before the 'until time'");
        }
    }

}
