package io.github.zero88.schedulerx.trigger;

import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.vertx.core.MultiMap;

@SuppressWarnings("unchecked")
public interface EventTriggerPredicate<T> extends Predicate<T> {

    default @Nullable T convert(@NotNull MultiMap header, @Nullable Object data) { return (T) data; }

    @Override
    boolean test(@Nullable T t);

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
     * Create an event trigger predicate that receives any kind of event will emit a trigger to start
     *
     * @param <T> type of event
     * @return the event trigger predicate
     */
    static <T> EventTriggerPredicate<T> any() {
        return ignoreType(t -> true);
    }

    static <T> EventTriggerPredicate<T> ignoreType(Predicate<T> predicate) {
        return new EventTriggerPredicate<T>() {
            @Override
            public @Nullable T convert(@NotNull MultiMap header, @Nullable Object data) {
                try {
                    return (T) data;
                } catch (ClassCastException ex) {
                    return null;
                }
            }

            @Override
            public boolean test(@Nullable T t) {
                return predicate.test(t);
            }
        };
    }

}
