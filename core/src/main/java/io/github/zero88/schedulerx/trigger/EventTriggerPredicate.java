package io.github.zero88.schedulerx.trigger;

import java.util.Optional;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.impl.Utils;
import io.vertx.core.MultiMap;

/**
 * An event trigger predicate
 *
 * @param <T> Type of event message
 */
public interface EventTriggerPredicate<T> extends Predicate<T> {

    /**
     * Convert message headers and body to an event message with desired type.
     *
     * @param headers message headers
     * @param body    message body
     * @return an event message
     */
    default @Nullable T convert(@NotNull MultiMap headers, @Nullable Object body) {
        return Utils.castOrNull(body, false);
    }

    /**
     * Evaluates this predicate on the given event message
     *
     * @param eventMessage the input argument
     * @return true if the event message argument matches the predicate, otherwise false
     */
    @Override
    boolean test(@Nullable T eventMessage);

    /**
     * Create an event trigger predicate that receives any kind of event will emit a trigger to start
     *
     * @param <T> type of event
     * @return the event trigger predicate
     */
    static <T> EventTriggerPredicate<T> any() {
        return ignoreType(t -> true, "Accept any event");
    }

    /**
     * Create an event trigger predicate by the given predicate
     *
     * @param <T> type of event
     * @return the event trigger predicate
     */
    static <T> EventTriggerPredicate<T> create(Predicate<T> predicate) {
        return predicate::test;
    }

    /**
     * Create an event trigger predicate allows any kind of event message type within the given predicate.
     *
     * @param <T> type of event
     * @return the event trigger predicate
     */
    static <T> EventTriggerPredicate<T> ignoreType(Predicate<T> predicate) {
        return ignoreType(predicate, null);
    }

    static <T> EventTriggerPredicate<T> ignoreType(@NotNull Predicate<T> predicate, @Nullable String toString) {
        return new EventTriggerPredicate<T>() {
            @Override
            public @Nullable T convert(@NotNull MultiMap headers, @Nullable Object body) {
                return Utils.castOrNull(body, true);
            }

            @Override
            public boolean test(@Nullable T eventMessage) { return predicate.test(eventMessage); }

            @Override
            public String toString() { return Optional.ofNullable(toString).orElseGet(super::toString); }
        };
    }

}
