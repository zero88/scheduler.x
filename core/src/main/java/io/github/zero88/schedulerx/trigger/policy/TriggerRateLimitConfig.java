package io.github.zero88.schedulerx.trigger.policy;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.ratelimit.RateLimitConfig;

/**
 * A rate-limit mechanism to control the number of execution rounds of one trigger can be run in parallel in a certain
 * period.
 *
 * @since 2.0.0
 */
public interface TriggerRateLimitConfig extends RateLimitConfig {

    @NotNull TriggerRateLimitKeyGenerator keyGenerator();

}
