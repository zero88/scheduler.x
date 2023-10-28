package io.github.zero88.schedulerx.trigger.rule;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

public class OffsetDateTimeTimeframe extends BaseTimeframe<OffsetDateTime> implements TimeRangeConstraint {

    @Override
    public final @NotNull Class<OffsetDateTime> type() { return OffsetDateTime.class; }

    @Override
    public boolean check(@NotNull Instant instant, @NotNull Duration leeway) {
        final ZoneOffset offset = Optional.ofNullable(from()).orElseGet(this::to).getOffset();
        final OffsetDateTime given = instant.atZone(offset).toOffsetDateTime();
        return (from() == null || given.isAfter(from())) && (to() == null || given.isBefore(to().plus(leeway)));
    }

    @Override
    public OffsetDateTime parse(Object value) {
        if (value instanceof CharSequence) {
            return OffsetDateTime.parse((CharSequence) value);
        }
        return (OffsetDateTime) value;
    }

    @Override
    public @NotNull TimeframeValidator validator() {
        return super.validator().and(this);
    }

}
