package io.github.zero88.ratelimit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represent the instruction to guide the down-stream system moves the current event in queue then retry processing it
 * based on throttle configuration and the current event context.
 */
public interface ThrottleInstruction<T> {

    /**
     * @return the throttle configuration
     */
    @NotNull ThrottleConfig throttleConfig();

    /**
     * @return the event context
     */
    @Nullable T context();

}
