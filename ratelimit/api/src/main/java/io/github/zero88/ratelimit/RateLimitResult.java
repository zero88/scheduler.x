package io.github.zero88.ratelimit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents for rate limit result after increasing the
 * <p/>
 * Within the next 19100 milliseconds, only 14 more requests are allowed by the SLA, which is set to allow 20 within
 * this time-window.
 *
 * @since 1.0.0
 */
public interface RateLimitResult<T> {

    /**
     * @return The policy unique key that represents for the trigger which to apply the rate limit
     */
    @NotNull Object key();

    /**
     * @return the rate-limit action
     */
    @NotNull RateLimitAction action();

    /**
     * @return The remaining number of events that can be run in parallel during the sliding time window
     */
    int remaining();

    /**
     * @return The maximum number of events that can be run in parallel during the sliding time window
     */
    int limit();

    /**
     * @return the remaining time, in milliseconds, until a new time window starts
     */
    long resetAfter();

    /**
     * @return the throttle instruction to queue and retry current event later
     */
    @Nullable ThrottleInstruction<T> throttleInstruction();

}
