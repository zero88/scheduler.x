package io.github.zero88.ratelimit.local;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.ratelimit.ConnectorConfig;
import io.github.zero88.ratelimit.RateLimitConfig;
import io.github.zero88.ratelimit.RateLimitPolicy;
import io.github.zero88.ratelimit.RateLimitPolicyRepository;
import io.github.zero88.ratelimit.RateLimitResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class RateLimitPolicyLocalRepository<T> implements RateLimitPolicyRepository<T> {

    private final ConcurrentMap<Object, RateLimitPolicy> store = new ConcurrentHashMap<>();
    private Vertx vertx;

    @Override
    public Future<RateLimitPolicyRepository<T>> setup(Vertx vertx) {
        this.vertx = vertx;
        return Future.succeededFuture(this);
    }

    @Override
    public Future<Void> register(Object policyKey, RateLimitConfig config, ConnectorConfig backendConfig) {
        if (store.containsKey(policyKey)) {
            return Future.succeededFuture();
        }
        return new LocalRateLimitPolicy(policyKey, config).initialize(vertx)
                                                          .map(policy -> store.put(policyKey, policy))
                                                          .map(rateLimitPolicy -> null);
    }

    @Override
    public Future<Void> unregister(Object policyKey) {
        final RateLimitPolicy policy = store.remove(policyKey);
        if (policy == null) {
            return Future.succeededFuture();
        }
        return policy.destroy();
    }

    @Override
    public @NotNull Future<RateLimitResult<T>> increase(@NotNull Object policyKey, @Nullable T eventContext) {
        final RateLimitPolicy policy = store.get(policyKey);
        if (policy == null) {
            return Future.failedFuture(new IllegalStateException("Not found policy key " + policyKey));
        }
        return policy.increase(eventContext);
    }

    @Override
    public Future<Void> decrease(@NotNull Object policyKey) {
        final RateLimitPolicy rateLimitPolicy = store.get(policyKey);
        if (rateLimitPolicy == null) {
            return Future.succeededFuture();
        }
        return rateLimitPolicy.destroy();
    }

}
