package io.github.zero88.schedulerx.spi;

import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.trigger.HasTriggerType;
import io.github.zero88.schedulerx.trigger.Trigger;

/**
 * @param <T> Type of trigger
 */
public interface TriggerRepresentationMapper<T extends Trigger>
    extends HasTriggerType, Function<T, TriggerRepresentation> {

    /**
     * Get the trigger representation from the given trigger.
     *
     * @param trigger the trigger
     * @return the trigger representation
     */
    TriggerRepresentation apply(@NotNull T trigger);

}
