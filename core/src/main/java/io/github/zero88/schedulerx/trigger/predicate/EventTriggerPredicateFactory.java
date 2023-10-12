package io.github.zero88.schedulerx.trigger.predicate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.impl.Utils;
import io.github.zero88.schedulerx.trigger.predicate.EventTriggerExtensionPredicate.MessageExtensionConverter;
import io.github.zero88.schedulerx.trigger.predicate.EventTriggerExtensionPredicate.MessageExtensionFilter;
import io.github.zero88.schedulerx.trigger.predicate.EventTriggerPredicate.JsonKey;
import io.github.zero88.schedulerx.trigger.predicate.EventTriggerPredicate.MessageConverter;
import io.github.zero88.schedulerx.trigger.predicate.EventTriggerPredicate.MessageFilter;

@SuppressWarnings("unchecked")
final class EventTriggerPredicateFactory {

    private EventTriggerPredicateFactory() { }

    static @NotNull EventTriggerPredicate<Object> ignoreType(@NotNull MessageFilter<Object> filter) {
        return create(AllowAnyMessageBodyType.INSTANCE, filter);
    }

    static <T> @NotNull EventTriggerPredicate<T> autoCast(@NotNull MessageFilter<T> filter) {
        return create(AutoCastMessageBody.create(), filter);
    }

    static <T> @NotNull EventTriggerPredicate<T> create(@NotNull MessageConverter<T> converter,
                                                        @NotNull MessageFilter<T> filter) {
        return new EventTriggerPredicateImpl<>(converter, filter);
    }

    static <T> @NotNull EventTriggerPredicate<T> create(Map<String, Object> properties) {
        Map<String, Object> props = Optional.ofNullable(properties).orElseGet(Collections::emptyMap);
        String predicateCls = getValue(props, JsonKey.EVENT_PREDICATE, EventTriggerPredicateImpl.class.getName());
        if (AnyEventTriggerPredicate.class.getName().equals(predicateCls)) {
            return (EventTriggerPredicate<T>) AnyEventTriggerPredicate.INSTANCE;
        }
        MessageConverter<T> converter = createMsgConverter(props);
        MessageFilter<T> filter = createMsgFilter(props);
        if (EventTriggerPredicateImpl.class.getName().equals(predicateCls)) {
            return new EventTriggerPredicateImpl<>(converter, filter);
        }
        return byExtension(predicateCls, converter, filter,
                           Utils.castOrNull(props.get(JsonKey.EVENT_PREDICATE_EXTRA), true));
    }

    @NotNull
    static <T> EventTriggerExtensionPredicate<T> byExtension(String predicateCls, MessageConverter<T> converter,
                                                             MessageFilter<T> filter, Map<String, Object> extraProps) {
        EventTriggerExtensionPredicate<T> predicate = initInstance(JsonKey.EVENT_PREDICATE,
                                                                   EventTriggerExtensionPredicate.class, predicateCls);
        Map<String, Object> args = new HashMap<>();
        args.put(JsonKey.MSG_CONVERTER, converter);
        args.put(JsonKey.MSG_FILTER, filter);
        args.put(JsonKey.EVENT_PREDICATE_EXTRA, extraProps);
        return tryLoad(predicate, args);
    }

    private static <T> MessageConverter<T> createMsgConverter(@NotNull Map<String, Object> props) {
        final String converterCls = getValue(props, JsonKey.MSG_CONVERTER, null);
        if (AllowAnyMessageBodyType.class.getName().equals(converterCls)) {
            return (MessageConverter<T>) AllowAnyMessageBodyType.INSTANCE;
        }
        if (AutoCastMessageBody.class.getName().equals(converterCls)) {
            return AutoCastMessageBody.create();
        }
        final MessageConverter<T> c = initInstance(JsonKey.MSG_CONVERTER, MessageConverter.class, converterCls);
        if (c instanceof MessageExtensionConverter) {
            return tryLoad((MessageExtensionConverter<T>) c,
                           Utils.castOrNull(props.get(JsonKey.MSG_CONVERTER_EXTRA), true));
        }
        return c;
    }

    private static <T> MessageFilter<T> createMsgFilter(@NotNull Map<String, Object> props) {
        final String filterCls = getValue(props, JsonKey.MSG_FILTER, null);
        final MessageFilter<T> f = initInstance(JsonKey.MSG_FILTER, MessageFilter.class, filterCls);
        if (f instanceof MessageExtensionFilter) {
            return tryLoad((MessageExtensionFilter<T>) f, Utils.castOrNull(props.get(JsonKey.MSG_FILTER_EXTRA), true));
        }
        return f;
    }

    @Nullable
    private static String getValue(@NotNull Map<String, Object> props, @NotNull String key, String defaultValue) {
        return Optional.ofNullable(props.getOrDefault(key, defaultValue)).map(Object::toString).orElse(null);
    }

    @SuppressWarnings("rawtypes")
    @NotNull
    private static <E extends ExtraPropertiesExtension> E tryLoad(@NotNull E instance,
                                                                  @Nullable Map<String, Object> extraProps) {
        try {
            return Objects.requireNonNull((E) instance.load(Optional.ofNullable(extraProps).orElseGet(Collections::emptyMap)));
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to setup " + instance.getClass(), e);
        }
    }

    @NotNull
    private static <T> T initInstance(@NotNull String key, @NotNull Class<T> expectedClass, @Nullable String givenCls) {
        if (givenCls == null || givenCls.trim().isEmpty()) {
            throw new IllegalArgumentException('\'' + key + "' is required");
        }
        try {
            Class<T> cls = (Class<T>) EventTriggerPredicateFactory.class.getClassLoader().loadClass(givenCls);
            if (expectedClass.isAssignableFrom(cls)) {
                return cls.getDeclaredConstructor().newInstance();
            }
            throw new ClassCastException();
        } catch (ClassCastException | ReflectiveOperationException e) {
            throw new IllegalArgumentException(
                "Invalid an argument class definition[" + givenCls + "], expecting a subclass of [" +
                expectedClass.getName() + "]", e);
        }
    }

}
