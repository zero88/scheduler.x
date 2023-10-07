package io.github.zero88.schedulerx.trigger.predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        return JsonObject.of(JsonKey.MSG_CONVERTER, converter.getClass().getName(),
                             JsonKey.MSG_FILTER, filter.getClass().getName());
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

}
