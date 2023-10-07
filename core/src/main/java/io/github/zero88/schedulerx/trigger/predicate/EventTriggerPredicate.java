package io.github.zero88.schedulerx.trigger.predicate;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * An event trigger predicate
 *
 * @param <T> Type of event message
 * @since 2.0.0
 */
public interface EventTriggerPredicate<T> extends Predicate<T> {

    /**
     * Convert message headers and body to an event message with desired type.
     *
     * @param headers message headers
     * @param body    message body
     * @return an event message
     */
    @Nullable T convert(@NotNull MultiMap headers, @Nullable Object body);

    /**
     * Evaluates this predicate on the given event message.
     * <p/>
     * This output is used to determine whether event trigger is executed or not.
     *
     * @param eventMessage the input argument
     * @return {@code true} if the event message argument matches the predicate, otherwise {@code false}
     */
    @Override
    boolean test(@Nullable T eventMessage);

    /**
     * Serialize this predicate to json.
     * <p/>
     * This method aims to serialize the predicate to a json data is able to persisted in any storage.
     * The deserialization is covered by {@link #create(Map)}.
     *
     * @return json object
     * @apiNote By default, the json serialization is not support to serialize an anonymous class or lambda
     * @see JsonKey
     */
    @JsonValue
    @NotNull JsonObject toJson();

    /**
     * Create an event trigger predicate that accepts any event to kick off a trigger.
     *
     * @return the event trigger predicate
     * @apiNote By using this factory method, the new event trigger predicate instance uses the {@code message body}
     *     as a predicate argument in {@link #test(Object)}
     * @see AnyEventTriggerPredicate
     */
    static EventTriggerPredicate<Object> any() {
        return AnyEventTriggerPredicate.INSTANCE;
    }

    /**
     * Create an event trigger predicate that accepts any of the event message types to satisfy the given filter.
     *
     * @return the event trigger predicate
     * @apiNote By using this factory method, the new event trigger predicate instance ignores the
     *     {@code message body} type and uses the message body as a filter argument in {@link #test(Object)}
     * @see AllowAnyMessageBodyType
     */
    static EventTriggerPredicate<Object> ignoreType(MessageFilter<Object> filter) {
        return EventTriggerPredicateFactory.ignoreType(filter);
    }

    /**
     * Create an event trigger predicate by the given filter.
     *
     * @param <T> type of event
     * @return the event trigger predicate
     * @apiNote By using this factory method, the new event trigger predicate instance casts the
     *     {@code message body} automatically to a desired type of event and uses the message body as a filter
     *     argument in {@link #test(Object)}.<br>
     *     Be aware that the cast operation might raise the {@code ClassCastException} in runtime and then make the
     *     trigger fail to execute.
     * @see AutoCastMessageBody
     */
    static <T> EventTriggerPredicate<T> create(MessageFilter<T> filter) {
        return EventTriggerPredicateFactory.autoCast(filter);
    }

    /**
     * Create an event trigger predicate by the given converter and given filter.
     *
     * @param <T>       type of event
     * @param converter the given message converter
     * @param filter    the given message filter
     * @return the event trigger predicate
     */
    static <T> EventTriggerPredicate<T> create(MessageConverter<T> converter, MessageFilter<T> filter) {
        return EventTriggerPredicateFactory.create(converter, filter);
    }

    /**
     * Deserialize the given properties to desired event trigger.
     *
     * @param properties the map properties
     * @param <T>        type of event
     * @return the event trigger predicate
     * @see JsonKey
     */
    @JsonCreator
    static <T> EventTriggerPredicate<T> create(Map<String, Object> properties) {
        return EventTriggerPredicateFactory.create(properties);
    }

    /**
     * The message converter
     *
     * @param <T> Type of event message
     */
    interface MessageConverter<T> extends BiFunction<MultiMap, Object, T> {

        /**
         * Convert the {@link Message} to desired event message
         *
         * @param headers the message header
         * @param body    the message body
         * @return an event message
         */
        @Override
        T apply(MultiMap headers, Object body);

    }


    /**
     * The message filter
     *
     * @param <T> Type of event message
     */
    interface MessageFilter<T> extends Predicate<T> {

        /**
         * Verify if the event message is satisfy desired condition.
         *
         * @param eventMessage the event message
         * @return {@code true} if satisfy, otherwise is {@code false}
         */
        @Override
        boolean test(T eventMessage);

    }


    /**
     * Declares the json key for EventTrigger predicate
     */
    class JsonKey {

        private JsonKey() { }

        public static final String EVENT_PREDICATE = "predicate";
        public static final String EVENT_PREDICATE_EXTRA = "predicateExtra";
        public static final String MSG_CONVERTER = "msgConverter";
        public static final String MSG_CONVERTER_EXTRA = "msgConverterExtra";
        public static final String MSG_FILTER = "msgFilter";
        public static final String MSG_FILTER_EXTRA = "msgFilterExtra";

    }

}
