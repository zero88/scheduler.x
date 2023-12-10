package io.github.zero88.ratelimit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * Represents a repository that keeps all rate-limit policies in a specific domain or category, like rate-limit policies
 * for the Restful APIs of one service, or rate-limit policies for a scheduler service, etc.
 *
 * @param <T> Type of event context lives in a specific domain
 */
public interface RateLimitPolicyRepository<T> {

    Future<RateLimitPolicyRepository<T>> setup(Vertx vertx);

    /**
     * Register new rate-limit policy
     *
     * @param policyKey     the policy key to identify a thing to apply rate-limiting
     * @param config        the rate-limit config
     * @param backendConfig the backend config for this policy
     * @return a future
     */
    Future<Void> register(Object policyKey, RateLimitConfig config, @Nullable ConnectorConfig backendConfig);

    /**
     * Unregister the rate-limit policy
     *
     * @param policyKey the policy key to identify a thing to apply rate-limiting
     * @return a future
     */
    Future<Void> unregister(Object policyKey);

    /**
     * Increase the event counter before new event is processed by the system
     *
     * @param policyKey    the policy key to identify a thing to apply rate-limiting
     * @param eventContext the event context of the incoming request
     * @return a rate-limit result
     * @see RateLimitResult
     * @see RateLimitKeyGenerator
     */
    @NotNull Future<RateLimitResult<T>> increase(@NotNull Object policyKey, @Nullable T eventContext);

    /**
     * Decrease the event counter when the event is finished
     *
     * @param policyKey the policy key to identify a thing to apply rate-limiting
     * @return
     */
    Future<Void> decrease(@NotNull Object policyKey);

}
