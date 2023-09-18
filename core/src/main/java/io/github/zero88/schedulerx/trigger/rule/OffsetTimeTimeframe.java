package io.github.zero88.schedulerx.trigger.rule;

import java.time.Instant;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

public class OffsetTimeTimeframe extends BaseTimeframe<OffsetTime> {

    @Override
    public final @NotNull Class<OffsetTime> type() { return OffsetTime.class; }

    @Override
    public OffsetTime parse(Object value) {
        if (value instanceof CharSequence) {
            return OffsetTime.parse((CharSequence) value);
        }
        return (OffsetTime) value;
    }

    @Override
    public boolean check(@NotNull Instant instant) {
        final ZoneOffset offset = Optional.ofNullable(from()).orElseGet(this::to).getOffset();
        final OffsetTime given = instant.atZone(offset).toOffsetDateTime().toOffsetTime();
        if (from() != null && to() != null && from().isAfter(to())) {
            return given.isAfter(from()) || given.isBefore(to());
        }
        return (from() == null || given.isAfter(from())) && (to() == null || given.isBefore(to()));
    }

}
