package io.github.zero88.schedulerx.spi;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.trigger.EventTrigger;

/**
 * @param <T> Type of event message
 */
public interface EventTriggerRepresentationMapper<T> extends TriggerRepresentationMapper<EventTrigger<T>> {

    @Override
    default @NotNull String type() { return EventTrigger.TRIGGER_TYPE; }

}
