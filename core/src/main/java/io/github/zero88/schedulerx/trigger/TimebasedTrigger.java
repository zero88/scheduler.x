package io.github.zero88.schedulerx.trigger;

import java.time.Instant;

import org.jetbrains.annotations.NotNull;

interface TimebasedTrigger {

    /**
     * Calculates the next valid trigger time from a given time and the trigger configuration
     *
     * @param time the given time
     * @return the next trigger time
     */
    @NotNull Instant nextTriggerTime(@NotNull Instant time);

}
