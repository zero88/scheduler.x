package io.github.zero88.schedulerx.trigger.rule;

import java.time.format.DateTimeParseException;

/**
 * Represents for a time parser
 *
 * @param <T> Type of time
 */
public interface TimeParser<T> {

    /**
     * Parses a given value to a time value in correct type.
     *
     * @param rawValue the input value from any source, e.g: json, external system, user input, etc.
     * @return the expected value
     * @throws DateTimeParseException if unable to parse the given value
     */
    T parse(Object rawValue);

}
