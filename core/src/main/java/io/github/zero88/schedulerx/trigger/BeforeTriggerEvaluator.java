package io.github.zero88.schedulerx.trigger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.vertx.core.Future;

/**
 * Represents for the trigger context checker on before trigger
 *
 * @since 2.0.0
 */
@FunctionalInterface
public interface BeforeTriggerEvaluator {

    /**
     * Verify if the trigger can run before each execution round is started.
     *
     * @param trigger        the trigger
     * @param triggerContext the trigger context
     * @param externalId     the job external id
     * @return a future of the trigger context that is evaluated
     */
    @NotNull Future<TriggerContext> beforeTrigger(@NotNull Trigger trigger, @NotNull TriggerContext triggerContext,
                                                  @Nullable Object externalId);

}
