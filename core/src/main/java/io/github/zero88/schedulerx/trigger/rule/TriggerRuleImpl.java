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

    TriggerRuleImpl(List<Timeframe> timeframes, Instant until) {
        this.timeframes = Optional.ofNullable(timeframes).orElseGet(Collections::emptyList);
        this.until      = until;
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
        int result = until.hashCode();
        result = 31 * result + timeframes.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TriggerRule{until=" + until + ", " + timeframes + "}";
    }

}
