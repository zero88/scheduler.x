package io.github.zero88.schedulerx.trigger.policy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.ratelimit.RateLimitPolicyRepository;
import io.github.zero88.ratelimit.RateLimitResult;
import io.github.zero88.schedulerx.trigger.TriggerContext;
import io.vertx.core.Future;

public interface TriggerRateLimitRepository extends RateLimitPolicyRepository<TriggerContext> {

    @NotNull Future<RateLimitResult<TriggerContext>> increase(@NotNull Object policyKey,
                                                              @Nullable TriggerContext eventContext);

}
