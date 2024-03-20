package io.github.zero88.ratelimit;

import org.jetbrains.annotations.NotNull;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * A rate-limit mechanism to control the maximum number of events can be processed in parallel in a certain period.
 *
 * @since 1.0.0
 */
public interface RateLimitPolicy {

    @NotNull Object policeKey();

    @NotNull RateLimitConfig config();

    /**
     * Set up the rate-limit policy
     *
     * @param vertx Vertx
     * @return a reference to this for fluent API
     */
    @NotNull Future<RateLimitPolicy> initialize(@NotNull Vertx vertx);

    Future<Void> destroy();

    /**
     * Increase the event counter before new event is processed by the system
     *
     * @return a rate-limit result
     * @see RateLimitResult
     */
    @NotNull <T> Future<RateLimitResult<T>> increase(T context);

    /**
     * Decrease the event counter when the event is finished
     */
    void decrease();

}
