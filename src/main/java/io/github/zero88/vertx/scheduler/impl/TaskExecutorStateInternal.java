package io.github.zero88.vertx.scheduler.impl;

import io.github.zero88.vertx.scheduler.TaskExecutorState;

import lombok.NonNull;

public interface TaskExecutorStateInternal extends TaskExecutorState {

    /**
     * Add timer id
     *
     * @param timerId timer id
     * @return this for fluent api
     */
    @NonNull TaskExecutorStateInternal timerId(long timerId);

    /**
     * Mark task is available to execute
     *
     * @return this for fluent api
     * @see #pending()
     */
    @NonNull TaskExecutorStateInternal markAvailable();

    /**
     * Mark task is executing
     *
     * @return this for fluent api
     * @see #executing()
     */
    @NonNull TaskExecutorStateInternal markExecuting();

    /**
     * Mark task is idle
     *
     * @return this for fluent api
     * @see #idle()
     */
    @NonNull TaskExecutorStateInternal markIdle();

    /**
     * Mark state is completed
     *
     * @return this for fluent api
     * @see #completed()
     */
    @NonNull TaskExecutorStateInternal markCompleted();

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
    Object addData(long round, Object data);

    /**
     * Add error when executing task per round
     *
     * @param round round
     * @param error error when executing task
     * @return current error, might be {@code null}
     */
    Throwable addError(long round, Throwable error);

}
