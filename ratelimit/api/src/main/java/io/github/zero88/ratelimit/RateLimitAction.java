package io.github.zero88.ratelimit;

/**
 * @since 1.0.0
 */
public enum RateLimitAction {
    /**
     * Accept to process new event
     */
    ACCEPTED,
    /**
     * Reject to process new event
     */
    REJECTED,
    /**
     * Move the new event into queue for retry later
     */
    THROTTLING
}
