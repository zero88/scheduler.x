package io.github.zero88.schedulerx.trigger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.vertx.core.Future;

/**
 * Represents for the trigger context checker on after trigger
 *
 * @since 2.0.0
 */
@FunctionalInterface
public interface AfterTriggerEvaluator {

    /**
     * Verify if the trigger should stop executing immediately after one round of execution begins.
     *
     * @param trigger        the trigger
     * @param triggerContext the trigger context
     * @param externalId     the job external id
     * @param round          the current execution round
     * @since 2.0.0
     */
    @NotNull Future<TriggerContext> afterTrigger(@NotNull Trigger trigger, @NotNull TriggerContext triggerContext,
                                                 @Nullable Object externalId, long round);

}
