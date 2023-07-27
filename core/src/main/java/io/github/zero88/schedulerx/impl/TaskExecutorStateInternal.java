package io.github.zero88.schedulerx.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.TaskExecutorState;

interface TaskExecutorStateInternal extends TaskExecutorState {

    /**
     * Add timer id
     *
     * @param timerId timer id
     * @return this for fluent api
     */
    @NotNull TaskExecutorStateInternal timerId(long timerId);

    /**
     * Mark task is available to execute
     *
     * @return this for fluent api
     * @see #pending()
     */
    @NotNull TaskExecutorStateInternal markAvailable();

    /**
     * Mark task is executing
     *
     * @return this for fluent api
     * @see #executing()
     */
    @NotNull TaskExecutorStateInternal markExecuting();

    /**
     * Mark task is idle
     *
     * @return this for fluent api
     * @see #idle()
     */
    @NotNull TaskExecutorStateInternal markIdle();

    /**
     * Mark state is completed
     *
     * @return this for fluent api
     * @see #completed()
     */
    @NotNull TaskExecutorStateInternal markCompleted();

    /**
     * Increase tick
     *
     * @return next tick
     */
    long increaseTick();

    /**
     * Increase round
     *
     * @return next round
     */
    long increaseRound();

    /**
     * Add task result data per round
     *
     * @param round round
     * @param data  data
     * @return current result data, might be {@code null}
     */
    @Nullable Object addData(long round, @Nullable Object data);

    /**
     * Add error when executing task per round
     *
     * @param round round
     * @param error error when executing task
     * @return current error, might be {@code null}
     */
    @Nullable Throwable addError(long round, @Nullable Throwable error);

}
