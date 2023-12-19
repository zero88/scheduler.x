package io.github.zero88.schedulerx.trigger;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

@Internal
public interface HasTriggerType {

    /**
     * Declares the trigger type
     *
     * @return the trigger type in string
     */
    @NotNull String type();

}
