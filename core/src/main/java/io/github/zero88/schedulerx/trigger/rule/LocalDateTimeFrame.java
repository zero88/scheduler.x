package io.github.zero88.schedulerx.trigger.rule;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.jetbrains.annotations.NotNull;

public class LocalDateTimeFrame extends BaseTimeframe<LocalDate> implements TimeRangeConstraint {

    @Override
    public final @NotNull Class<LocalDate> type() { return LocalDate.class; }

    @Override
    public boolean check(@NotNull Instant instant, @NotNull Duration leeway) {
        final LocalDate given = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        return (from() == null || given.isAfter(from())) && (to() == null || given.isBefore(to()));
    }

    @Override
    public LocalDate parse(Object value) {
        if (value instanceof CharSequence) {
            return LocalDate.parse((CharSequence) value);
        }
        return (LocalDate) value;
    }

    @Override
    public @NotNull TimeframeValidator validator() {
        return super.validator().and(this);
    }

}
