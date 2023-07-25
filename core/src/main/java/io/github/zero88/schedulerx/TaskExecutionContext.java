package io.github.zero88.schedulerx;

import java.time.Instant;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;

/**
 * Represents for a context per each execution round
 *
 * @since 1.0.0
 */
public interface TaskExecutionContext {

    /**
     * Setup task execution context
     *
     * @param promise    promise
     * @param executedAt execution at time
     * @return a reference to this for fluent API
     * @apiNote It will be invoked by system. In any attempts invoking, {@link IllegalStateException} will be
     *     thrown
     * @see Promise
     */
    @NotNull TaskExecutionContext setup(@NotNull Promise<Object> promise, @NotNull Instant executedAt);

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
     * Mark a flag stop/cancel to cancel executor
     */
    void forceStopExecution();

    /**
     * Completed execution with result data per each round
     *
     * @param data object data
     * @apiNote if task is {@code async} then it should be invoked in handling async result stage
     */
    void complete(@Nullable Object data);

    /**
     * Failed execution with error per each round
     *
     * @param throwable execution error
     * @apiNote if task is {@code async} then it should be invoked in handling async result stage
     */
    void fail(@Nullable Throwable throwable);

}
