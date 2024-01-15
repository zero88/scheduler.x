package io.github.zero88.schedulerx;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.trigger.Trigger;

@Internal
interface HasTrigger<TRIGGER extends Trigger> {

    /**
     * The trigger
     *
     * @return trigger
     * @see Trigger
     */
    @NotNull TRIGGER trigger();

}
