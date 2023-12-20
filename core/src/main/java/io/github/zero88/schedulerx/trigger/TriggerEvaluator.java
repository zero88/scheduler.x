package io.github.zero88.schedulerx.trigger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.vertx.core.Future;

/**
 * Represents for the trigger evaluator to assess whether the trigger is able to run
 *
 * @since 2.0.0
 */
public interface TriggerEvaluator {

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

    /**
     * Verify if the trigger should stop executing immediately after one round of execution begins.
     *
     * @param round the current execution round
     * @since 2.0.0
     */
    @NotNull Future<TriggerContext> afterTrigger(@NotNull Trigger trigger, @NotNull TriggerContext triggerContext,
                                                 @Nullable Object externalId, long round);

    /**
     * Chain another evaluator
     *
     * @param another another evaluator
     * @return a reference to this for fluent API
     */
    @NotNull TriggerEvaluator andThen(@Nullable TriggerEvaluator another);

}
