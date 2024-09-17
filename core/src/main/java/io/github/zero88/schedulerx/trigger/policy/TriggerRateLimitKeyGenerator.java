package io.github.zero88.schedulerx.trigger.policy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.ratelimit.RateLimitKeyGenerator;
import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.trigger.Trigger;

public interface TriggerRateLimitKeyGenerator extends RateLimitKeyGenerator {

    boolean requireExternalKey();

    /**
     * Generate rate-limit policy key for the trigger
     *
     * @param trigger     the trigger.
     * @param externalKey the external trigger key. See {@link JobData#externalId()}
     * @return the unique key in the rate-limit system
     * @apiNote to identify a trigger to apply rate-limiting, the concrete key generator can combine some properties
     *     such as {@code externalKey}, {@code triggerType} and/or {@code triggerContextInfo}.
     * @see Trigger
     */
    @NotNull Object generate(@NotNull Trigger trigger, @Nullable Object externalKey);

}
