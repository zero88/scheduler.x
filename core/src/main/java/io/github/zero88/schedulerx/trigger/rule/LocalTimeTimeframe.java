package io.github.zero88.schedulerx.trigger.rule;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

import org.jetbrains.annotations.NotNull;

public class LocalTimeTimeframe extends BaseTimeframe<LocalTime> {

    @Override
    public final @NotNull Class<LocalTime> type() { return LocalTime.class; }

    @Override
    public LocalTime parse(Object value) {
        if (value instanceof CharSequence) {
            return LocalTime.parse((CharSequence) value);
        }
        return (LocalTime) value;
    }

    @Override
    public boolean check(@NotNull Instant instant, @NotNull Duration leeway) {
        final LocalTime given = instant.atZone(ZoneId.systemDefault()).toLocalTime();
        if (from() != null && to() != null && from().isAfter(to())) {
            return given.isAfter(from()) || given.isBefore(to().plus(leeway));
        }
        return (from() == null || given.isAfter(from())) && (to() == null || given.isBefore(to().plus(leeway)));
    }

}
