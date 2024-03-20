package io.github.zero88.ratelimit;

import java.time.Duration;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import io.vertx.core.json.JsonObject;

/**
 * A rate-limit mechanism to control the maximum number of events can be processed in parallel in a certain period.
 *
 * @since 1.0.0
 */
public interface RateLimitConfig {

    /**
     * Declares the set of limit configuration
     *
     * @return the limit configuration
     * @see LimitConfig
     */
    @NotNull List<LimitConfig> limits();

    /**
     * Declares a request timeout when increasing the rate-limit counter
     *
     * @return the timeout
     */
    @NotNull Duration timeout();

    /**
     * Declares the generator to generate the rate-limit policy key.
     *
     * @return the key generator
     * @see RateLimitKeyGenerator
     */
    @NotNull RateLimitKeyGenerator keyGenerator();

    @NotNull JsonObject toJson();

}
