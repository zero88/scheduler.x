package io.github.zero88.schedulerx;

import java.time.Instant;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

/**
 * Represents for a management state of the execution lifecycle.
 *
 * @param <OUT> Type of task result data
 * @apiNote This interface is renamed from {@code TaskExecutorState} since {@code 2.0.0}
 * @since 2.0.0
 */
@Internal
public interface SchedulerState<OUT> {

    /**
     * @return The system timer id
     */
    long timerId();

    /**
     * Identify the trigger is registered on the system timer at a clock time.
     *
     * @return available at time
     */
    Instant availableAt();

    /**
     * @return The current number of times that the system timer fires the trigger.
     */
    long tick();

    /**
     * @return The current number of times that the trigger is executed.
     */
    long round();

    /**
     * Check whether the trigger is in {@code pending} state that means is not yet registered in a {@code scheduler}.
     *
     * @return {@code true} if pending
     */
    boolean pending();

    /**
     * Check whether the trigger is in processing: validation phase or execution phase.
     *
     * @return {@code true} if in progress
     */
    boolean executing();

    /**
     * Check whether the trigger is {@code completed} state that means safe to remove out of a {@code scheduler}.
     *
     * @return {@code true} if completed
     */
    boolean completed();

    /**
     * Get the data of latest round.
     *
     * @return latest data
     */
    @Nullable OUT lastData();

    /**
     * Get the error of latest round.
     *
     * @return latest error
     */
    @Nullable Throwable lastError();

}
