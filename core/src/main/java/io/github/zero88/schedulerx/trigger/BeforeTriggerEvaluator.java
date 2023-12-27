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
     * Verify if the trigger can run before each execution round is started, then transition the trigger context to a
     * desired state from {@code KICKOFF} to {@code READY} or {@code MISFIRE}.
     * <p/>
     * Only final state is {@code READY}, the trigger will be scheduled to execute, other states will emit a trigger
     * misfire event.
     *
     * @param trigger        the trigger
     * @param triggerContext the trigger context
     * @param externalId     the job external id
     * @return a future of the transition trigger context that is evaluated
     * @implNote If the {@code triggerContext} has status {@link TriggerContext#isReadiness()} is {@code false},
     *     the evaluation operation is stopped immediately, means the follow evaluators will not be invoked.
     */
    @NotNull Future<TriggerContext> beforeTrigger(@NotNull Trigger trigger, @NotNull TriggerContext triggerContext,
                                                  @Nullable Object externalId);

}
