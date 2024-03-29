package io.github.zero88.schedulerx.trigger.rule;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.jetbrains.annotations.NotNull;

public class LocalDateTimeRange extends BaseTimeframe<LocalDateTime> implements TimeRangeConstraint {

    @Override
    public final @NotNull Class<LocalDateTime> type() { return LocalDateTime.class; }

    @Override
    public boolean check(@NotNull Instant instant, @NotNull Duration leeway) {
        final LocalDateTime given = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        return (from() == null || !given.isBefore(from())) && (to() == null || given.isBefore(to().plus(leeway)));
    }

    @Override
    public LocalDateTime parse(Object value) {
        if (value instanceof CharSequence) {
            return LocalDateTime.parse((CharSequence) value);
        }
        return (LocalDateTime) value;
    }

    @Override
    public @NotNull TimeframeValidator validator() {
        return super.validator().and(this);
    }

}
