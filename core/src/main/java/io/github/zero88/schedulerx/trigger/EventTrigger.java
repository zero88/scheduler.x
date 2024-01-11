package io.github.zero88.schedulerx.trigger;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.trigger.predicate.EventTriggerPredicate;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Represents for inspecting settings specific to an Event trigger.
 * <p/>
 * An event trigger is a schedule that runs on receiving a specific event from a {@code Vert.x event-bus} distributed
 * messaging system, in short it acts as one of subscriber in publish/subscribe (pub/sub) pattern or a message consumer
 * in Producer-Consumer pattern.
 *
 * @param <T> Type of event message
 * @since 2.0.0
 */
@JsonDeserialize(builder = EventTriggerBuilder.class)
public interface EventTrigger<T> extends Trigger {

    String TRIGGER_TYPE = "event";

    static <T> EventTriggerBuilder<T> builder() { return new EventTriggerBuilder<>(); }

    @Override
    default @NotNull String type() { return TRIGGER_TYPE; }

    /**
     * Declares a flag to check whether the event trigger listens to the event from the cluster or only local.
     *
     * @see EventBus#localConsumer(String)
     */
    boolean isLocalOnly();

    /**
     * Declares the event-bus specific address
     */
    @NotNull String getAddress();

    /**
     * Declares the predicate to filter the incoming event
     *
     * @see EventTriggerPredicate
     */
    @NotNull EventTriggerPredicate<T> getPredicate();

    @Override
    default @NotNull EventTrigger<T> validate() { return this; }

    @Override
    default @NotNull List<OffsetDateTime> preview(@NotNull PreviewParameter parameter) { return new ArrayList<>(); }

    @Override
    default JsonObject toJson() {
        JsonObject self = JsonObject.of("address", getAddress(), "localOnly", isLocalOnly(), "eventTriggerPredicate",
                                        getPredicate().toJson());
        return Trigger.super.toJson().mergeIn(self);
    }

}
