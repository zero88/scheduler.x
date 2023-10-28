package io.github.zero88.schedulerx.impl;

import java.time.Instant;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.trigger.TriggerContext;

@Internal
public interface TriggerTransitionContext extends TriggerContext {

    /**
     * @return The current number of times that the system timer fires the trigger.
     */
    long tick();

    /**
     * The time that the system timer fires the trigger.
     */
    @Nullable Instant firedAt();

}
