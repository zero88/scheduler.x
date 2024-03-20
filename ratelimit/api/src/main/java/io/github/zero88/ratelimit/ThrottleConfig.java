package io.github.zero88.ratelimit;

import java.time.Duration;

import org.jetbrains.annotations.NotNull;

/**
 * Represents for the throttling capability to queue and retry events
 *
 * @since 1.0.0
 */
public interface ThrottleConfig {

    /**
     * Defines the period time that the system retries to handle the event periodically until the rate-limit system
     * accepts the event to process.
     *
     * @return the retry after
     */
    @NotNull Duration retryAfter();

    /**
     * Defines the maximum amount of times to retry until the rate-limit system accepts the event to process.
     * After reach the maximum times, the rate-limit system will reject to handle the event.
     *
     * @return the max retry time
     */
    int maxRetryTimes();

}
