package io.github.zero88.schedulerx.trigger.predicate;

import io.vertx.core.MultiMap;

public final class AutoCastMessageBody<T> implements EventTriggerPredicate.MessageConverter<T> {

    private AutoCastMessageBody()                                        { }

    public static <T> EventTriggerPredicate.MessageConverter<T> create() { return new AutoCastMessageBody<>(); }

    @Override
    @SuppressWarnings("unchecked")
    public T apply(MultiMap headers, Object body) { return (T) body; }

    @Override
    public int hashCode() {
        return AutoCastMessageBody.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == AutoCastMessageBody.class;
    }

}
