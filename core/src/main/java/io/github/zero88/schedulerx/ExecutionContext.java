package io.github.zero88.schedulerx;

import java.time.Instant;

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
    @NotNull Instant triggeredAt();

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
     * Mark a force stop flag to unregister the trigger out of the system timer
     */
    void forceStopExecution();

    /**
     * Notify finish an execution per each round with its result data
     *
     * @param data object data
     * @apiNote if task is {@code async} then it should be invoked in handling async result stage
     */
    void complete(@Nullable OUT data);

    /**
     * Notify finish an execution per each round with its error data
     *
     * @param throwable execution error
     * @apiNote if task is {@code async} then it should be invoked in handling async result stage
     */
    void fail(@Nullable Throwable throwable);

}
