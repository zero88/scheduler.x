package io.github.zero88.schedulerx.trigger.predicate;

import io.vertx.core.MultiMap;

public final class AllowAnyMessageBodyType implements EventTriggerPredicate.MessageConverter<Object> {

    private AllowAnyMessageBodyType() { }

    public static final EventTriggerPredicate.MessageConverter<Object> INSTANCE = new AllowAnyMessageBodyType();

    @Override
    public Object apply(MultiMap headers, Object body) { return body; }

}
