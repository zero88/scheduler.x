package io.github.zero88.schedulerx.trigger.rule;

import java.time.Duration;
import java.time.Instant;

import org.jetbrains.annotations.NotNull;

public class InstantTimeframe extends BaseTimeframe<Instant> implements TimeRangeConstraint {

    @Override
    public final @NotNull Class<Instant> type() { return Instant.class; }

    @Override
    public boolean check(@NotNull Instant instant, @NotNull Duration leeway) {
        return (from() == null || instant.isAfter(from())) && (to() == null || instant.isBefore(to().plus(leeway)));
    }

    @Override
    public Instant parse(Object value) {
        if (value instanceof CharSequence) {
            return Instant.parse((CharSequence) value);
        }
        return (Instant) value;
    }

    @Override
    public @NotNull TimeframeValidator validator() {
        return super.validator().and(this);
    }

}
