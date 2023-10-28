package io.github.zero88.schedulerx.impl;

import java.time.Instant;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.SchedulerState;

interface SchedulerStateInternal<OUTPUT> extends SchedulerState<OUTPUT> {

    /**
     * Add current timer id
     *
     * @param timerId timer id
     * @return this for fluent api
     */
    @NotNull SchedulerStateInternal<OUTPUT> timerId(long timerId);

    /**
     * Mark the trigger is already registered in the system timer.
     *
     * @return available at time
     * @see #pending()
     */
    @NotNull Instant markAvailable();

    /**
     * Mark trigger tick is already handled
     *
     * @param tick the trigger tick
     * @return finished at time
     */
    @NotNull Instant markFinished(long tick);

    /**
     * Mark the current trigger is completed, no more fire by the system timer.
     *
     * @return completed at time
     */
    @NotNull Instant markCompleted();

    /**
     * Increase the tick counter and mark the current tick is in progress
     *
     * @return next tick
     */
    long increaseTick();

    /**
     * Increase the round counter
     *
     * @return next round
     */
    long increaseRound();

    /**
     * Add the task result data per round
     *
     * @param round round
     * @param data  data
     * @return current result data, might be {@code null}
     */
    @Nullable OUTPUT addData(long round, @Nullable OUTPUT data);

    /**
     * Add the task result error per round
     *
     * @param round round
     * @param error error when executing task
     * @return current error, might be {@code null}
     */
    @Nullable Throwable addError(long round, @Nullable Throwable error);

}
