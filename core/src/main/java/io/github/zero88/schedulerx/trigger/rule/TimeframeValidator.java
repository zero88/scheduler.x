package io.github.zero88.schedulerx.trigger.rule;

import java.time.DateTimeException;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

/**
 * Represents for a validator to ensure Timeframe is valid to use.
 *
 * @since 2.0.0
 */
public interface TimeframeValidator {

    TimeframeValidator BASE = timeframe -> {
        if (timeframe.from() == null && timeframe.to() == null) {
            throw new IllegalArgumentException(
                "Required at least one non-null value on either minimum or maximum value");
        }
        if (timeframe.from() != null && timeframe.to() != null &&
            timeframe.from().getClass() != timeframe.to().getClass()) {
            throw new IllegalArgumentException("Minimum and maximum value are not same type");
        }
    };

    void validate(Timeframe timeFrame);

    default <T> T normalize(@NotNull TimeParser<T> parser, Object rawValue) {
        try {
            return parser.parse(rawValue);
        } catch (ClassCastException ex) {
            throw new DateTimeException("Unsupported input type[" + rawValue.getClass().getName() + "]");
        }
    }

    default TimeframeValidator and(@NotNull TimeframeValidator other) {
        Objects.requireNonNull(other);
        return timeFrame -> {
            validate(timeFrame);
            other.validate(timeFrame);
        };
    }

}
