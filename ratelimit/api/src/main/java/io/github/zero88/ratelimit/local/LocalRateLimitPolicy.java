package io.github.zero88.ratelimit.local;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.ratelimit.RateLimitConfig;
import io.github.zero88.ratelimit.RateLimitPolicy;
import io.github.zero88.ratelimit.RateLimitResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class LocalRateLimitPolicy implements RateLimitPolicy {

    private final Object policyKey;
    private final RateLimitConfig config;

    public LocalRateLimitPolicy(Object policyKey, RateLimitConfig config) {
        this.policyKey = Objects.requireNonNull(policyKey);
        this.config    = Objects.requireNonNull(config);
    }

    @Override
    public @NotNull Object policeKey() { return policyKey; }

    @Override
    public @NotNull RateLimitConfig config() { return config; }

    @Override
    public @NotNull Future<RateLimitPolicy> initialize(@NotNull Vertx vertx) {
        return Future.succeededFuture(this);
    }

    @Override
    public Future<Void> destroy() {
        return Future.succeededFuture();
    }

    @Override
    public @NotNull <T> Future<RateLimitResult<T>> increase(T context) {
        return null;
    }

    @Override
    public void decrease() {

    }

}
