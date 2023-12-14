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
     * Check whether the trigger is able to run
     *
     * @param trigger    the trigger
     * @param context    the trigger context
     * @param externalId the job external id
     * @return a future of the trigger context that is evaluated
     */
    @NotNull Future<TriggerContext> beforeRun(@NotNull Trigger trigger, @NotNull TriggerContext context,
                                              @Nullable Object externalId);

    /**
     * Chain another evaluator
     *
     * @param another another evaluator
     * @return a reference to this for fluent API
     */
    @NotNull TriggerEvaluator andThen(@Nullable TriggerEvaluator another);

}
