package io.github.zero88.schedulerx.trigger.rule;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * A particular period of time in which a trigger can be emitted.
 *
 * @param <T> Type of time frame value
 * @since 2.0.0
 */
public interface Timeframe<T> {

    /**
     * Obtains an instance of Timeframe by {@code from}.
     *
     * @param from a minimum allowed value (inclusive)
     * @param <T>  Type of time frame value
     * @param <TF> Type of time frame
     * @return new instance of timeframe that allows a time starts from to the given {@code from} value
     * @throws NullPointerException if the given {@code from} value is null
     */
    static @NotNull <T, TF extends Timeframe<T>> TF from(@NotNull T from) {
        return of(Objects.requireNonNull(from), null);
    }

    /**
     * Obtains an instance of Timeframe by {@code to}.
     *
     * @param to   a maximum allowed value (exclusive)
     * @param <T>  Type of time frame value
     * @param <TF> Type of time frame
     * @return new instance of timeframe that allows a time is until to the given {@code to} value
     * @throws NullPointerException if the given {@code to} value is null
     */
    static @NotNull <T, TF extends Timeframe<T>> TF to(@NotNull T to) {
        return of(null, Objects.requireNonNull(to));
    }

    /**
     * Obtains an instance of Timeframe by {@code from} value and {@code to} value.
     *
     * @param from a minimum allowed value (inclusive)
     * @param to   a maximum allowed value (exclusive)
     * @param <T>  Type of time frame value
     * @param <TF> Type of time frame
     * @return new instance of timeframe, not null
     * @throws IllegalArgumentException      if unable to parse the given values
     * @throws UnsupportedOperationException if unknown timeframe type
     */
    static @NotNull <T, TF extends Timeframe<T>> TF of(T from, T to) {
        return create(Optional.ofNullable(Optional.ofNullable(from).orElse(to))
                              .map(Object::getClass)
                              .map(Class::getName)
                              .orElse(null), from, to);
    }

    /**
     * Obtains an instance of Timeframe by {@code type} of timeframe value, and the agnostic values: {@code from} and
     * {@code to}.
     *
     * @param type a type of timeframe value
     * @param from a minimum allowed value (inclusive)
     * @param to   a maximum allowed value (exclusive)
     * @param <TF> Type of timeframe
     * @return new instance of timeframe, not null
     * @throws IllegalArgumentException      if unable to parse the given values
     * @throws UnsupportedOperationException if unknown timeframe type
     */
    static <TF extends Timeframe<?>> TF create(String type, Object from, Object to) {
        //noinspection unchecked
        return (TF) TimeframeFactory.getInstance().create(type, from, to, null);
    }

    /**
     * Obtains an instance of Timeframe
     *
     * @param properties the arbitrary key value from json
     * @param <TF>       Type of timeframe
     * @return new instance of timeframe, not null
     * @apiNote This method supports Jackson deserialization
     */
    @JsonCreator
    static <TF extends Timeframe<?>> TF create(Map<String, Object> properties) {
        //noinspection unchecked
        return (TF) TimeframeFactory.getInstance().create(properties);
    }

    /**
     * @return type of timeframe value, should be a subclass of {@code Temporal}
     */
    @JsonGetter
    @NotNull Class<T> type();

    /**
     * @return minimum allowed value (inclusive)
     */
    @JsonGetter
    @Nullable T from();

    /**
     * @return maximum allowed value (exclusive)
     */
    @JsonGetter
    @Nullable T to();

    /**
     * Verify the given time is in range of {@link #from()} and {@link #to()} within allowable time margin.
     *
     * @param instant a given time that is an instantaneous point on the time-line
     * @param leeway  a leeway time
     * @return {@code true} if the given time is in range, otherwise is {@code false}
     */
    boolean check(@NotNull Instant instant, @NotNull Duration leeway);

    /**
     * @return a time parser
     */
    @NotNull TimeParser<T> parser();

    /**
     * @return a timeframe validator
     */
    default @NotNull TimeframeValidator validator() { return TimeframeValidator.BASE; }

    /**
     * Set property value in the timeframe.
     *
     * @param field the timeframe field. E.g: "from", "to", and other extra proprieties
     * @param value the property value
     * @return a reference to this for fluent API
     * @apiNote This method is useful to implement the custom timeframe. When using this method with the builtin
     *     timeframe, {@link UnsupportedOperationException} will be thrown
     */
    @NotNull Timeframe<T> set(@NotNull String field, @Nullable Object value);

}
