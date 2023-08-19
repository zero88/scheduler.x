package io.github.zero88.schedulerx;

import java.time.Instant;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.trigger.TriggerContext;
import io.vertx.core.Vertx;

/**
 * Represents for a runtime context per each execution round.
 *
 * @param <OUT> Type of task result data
 * @apiNote This interface is renamed from {@code TaskExecutionContext} since {@code 2.0.0}
 * @since 1.0.0
 */
public interface ExecutionContext<OUT> {

    /**
     * Current vertx
     *
     * @return vertx
     */
    @NotNull Vertx vertx();

    /**
     * Current execution round
     *
     * @return round
     */
    long round();

    /**
     * Runtime trigger context
     *
     * @see TriggerContext
     * @since 2.0.0
     */
    @NotNull TriggerContext triggerContext();

    /**
     * Trigger execution at time
     *
     * @return triggeredAt
     */
    default @NotNull Instant triggeredAt() {
        return Objects.requireNonNull(triggerContext().triggerAt());
    }

    /**
     * Executed at time
     *
     * @return executedAt
     */
    @NotNull Instant executedAt();

    /**
     * Check whether force stop execution or not
     *
     * @return {@code true} if force stop
     */
    boolean isForceStop();

    /**
     * Mark a flag stop/cancel to cancel executor
     */
    void forceStopExecution();

    /**
     * Completed execution with result data per each round
     *
     * @param data object data
     * @apiNote if task is {@code async} then it should be invoked in handling async result stage
     */
    void complete(@Nullable OUT data);

    /**
     * Failed execution with error per each round
     *
     * @param throwable execution error
     * @apiNote if task is {@code async} then it should be invoked in handling async result stage
     */
    void fail(@Nullable Throwable throwable);

}
