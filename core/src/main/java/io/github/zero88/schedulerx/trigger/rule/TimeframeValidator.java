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
        return timeframe;
    };

    /**
     * Validate the given timeframe
     *
     * @param timeframe the give timeframe
     * @return the timeframe if valid, otherwise throw {@link IllegalArgumentException}
     */
    @SuppressWarnings("rawtypes")
    Timeframe validate(Timeframe timeframe);

    /**
     * Normalize the given input to expected value with correct type
     *
     * @param parser   the time parser
     * @param rawValue the given input
     * @param <T>      Type of timeframe value
     * @return an expected type
     */
    default <T> T normalize(@NotNull TimeParser<T> parser, Object rawValue) {
        try {
            return parser.parse(rawValue);
        } catch (ClassCastException ex) {
            throw new DateTimeException("Unsupported input type[" + rawValue.getClass().getName() + "]");
        }
    }

    default TimeframeValidator and(@NotNull TimeframeValidator other) {
        Objects.requireNonNull(other);
        return timeframe -> other.validate(validate(timeframe));
    }

}
