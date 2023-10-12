package io.github.zero88.schedulerx.trigger.predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

public final class AnyEventTriggerPredicate implements EventTriggerPredicate<Object> {

    private AnyEventTriggerPredicate() { }

    public static final EventTriggerPredicate<Object> INSTANCE = new AnyEventTriggerPredicate();

    @Override
    public @Nullable Object convert(@NotNull MultiMap headers, @Nullable Object body) { return body; }

    @Override
    public boolean test(@Nullable Object eventMessage) { return true; }

    @Override
    public @NotNull JsonObject toJson() {
        return JsonObject.of(JsonKey.EVENT_PREDICATE,
                             AnyEventTriggerPredicate.class.getName());
    }

    @Override
    public String toString() { return "Accept any event"; }

}
