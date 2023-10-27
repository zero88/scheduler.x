package io.github.zero88.schedulerx.trigger.rule;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("rawtypes")
final class TriggerRuleImpl implements TriggerRule {

    private final List<Timeframe> timeframes;
    private final Instant until;
    private final int hashCode;

    TriggerRuleImpl(List<Timeframe> timeframes, Instant until) {
        this.timeframes = Optional.ofNullable(timeframes).orElseGet(Collections::emptyList);
        this.until      = until;
        this.hashCode   = computeHashCode();
    }

    @Override
    public @NotNull List<Timeframe> timeframes() { return timeframes; }

    @Override
    public Instant until() { return until; }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        TriggerRuleImpl that = (TriggerRuleImpl) o;
        return Objects.equals(until, that.until) && timeframes.equals(that.timeframes);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "TriggerRule{until=" + until + ", " + timeframes + "}";
    }

    private int computeHashCode() {
        int result = Optional.ofNullable(until).map(Instant::hashCode).orElse(0);
        result = 31 * result + timeframes.hashCode();
        return result;
    }

}
