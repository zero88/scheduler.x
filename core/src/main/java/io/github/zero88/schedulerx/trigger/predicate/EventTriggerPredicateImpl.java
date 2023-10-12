package io.github.zero88.schedulerx.trigger.predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.trigger.predicate.EventTriggerExtensionPredicate.MessageExtensionConverter;
import io.github.zero88.schedulerx.trigger.predicate.EventTriggerExtensionPredicate.MessageExtensionFilter;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

final class EventTriggerPredicateImpl<T> implements EventTriggerPredicate<T> {

    private final MessageConverter<T> converter;
    private final MessageFilter<T> filter;

    EventTriggerPredicateImpl(@NotNull MessageConverter<T> converter, @NotNull MessageFilter<T> filter) {
        this.converter = converter;
        this.filter    = filter;
    }

    @Override
    public @Nullable T convert(@NotNull MultiMap headers, @Nullable Object body) {
        return converter.apply(headers, body);
    }

    @Override
    public boolean test(@Nullable T eventMessage) {
        return filter.test(eventMessage);
    }

    @Override
    public @NotNull JsonObject toJson() {
        if (isAnonymousClassOrLambda(converter) || isAnonymousClassOrLambda(filter)) {
            throw new UnsupportedOperationException("Unable to serialize anonymous class or lambda");
        }
        final JsonObject json = JsonObject.of(JsonKey.MSG_CONVERTER, converter.getClass().getName(), JsonKey.MSG_FILTER,
                                              filter.getClass().getName());
        if (converter instanceof MessageExtensionConverter) {
            putExtraData(json, ((MessageExtensionConverter<T>) converter).extra(), JsonKey.MSG_CONVERTER_EXTRA);
        }
        if (filter instanceof MessageExtensionFilter) {
            putExtraData(json, ((MessageExtensionFilter<T>) filter).extra(), JsonKey.MSG_FILTER_EXTRA);
        }
        return json;
    }

    private boolean isAnonymousClassOrLambda(Object o) {
        final Class<?> aClass = o.getClass();
        return aClass.isAnonymousClass() || aClass.getName().contains("$$Lambda$");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        EventTriggerPredicateImpl<?> that = (EventTriggerPredicateImpl<?>) o;
        return converter.equals(that.converter) && filter.equals(that.filter);
    }

    @Override
    public int hashCode() {
        int result = converter.hashCode();
        result = 31 * result + filter.hashCode();
        return result;
    }

    private static void putExtraData(JsonObject output, JsonObject extra, String msgFilterExtra) {
        if (extra != null && !extra.isEmpty()) {
            output.put(msgFilterExtra, extra);
        }
    }

}
