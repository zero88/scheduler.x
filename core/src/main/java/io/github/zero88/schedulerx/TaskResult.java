package io.github.zero88.schedulerx;

import java.time.Instant;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

/**
 * Represents for task result will be pass on each event of {@link TaskExecutorMonitor}
 *
 * @see TaskExecutorMonitor
 * @since 1.0.0
 */
public interface TaskResult {

    /**
     * Only {@code not null} in {@link TaskExecutorMonitor#onUnableSchedule(TaskResult)}
     *
     * @return unschedule at time
     */
    Instant unscheduledAt();

    /**
     * Only {@code not null} if reschedule {@link TaskExecutorMonitor#onUnableSchedule(TaskResult)}
     *
     * @return reschedule at time
     * @see #isReschedule()
     */
    Instant rescheduledAt();

    /**
     * Task executor is available to run, in other words "on {@code scheduler}" at time
     *
     * @return available at time
     */
    Instant availableAt();

    /**
     * Task executor triggers task round at time
     *
     * @return task round triggered at time
     */
    Instant triggeredAt();

    /**
     * Task executor executes task round at time
     *
     * @return task round executed at time
     */
    Instant executedAt();

    /**
     * Task executor finishes task round at time
     *
     * @return task round finished at time
     */
    Instant finishedAt();

    /**
     * Task executor completes at time
     *
     * @return completed at time
     */
    Instant completedAt();

    /**
     * The execution tick
     *
     * @return the execution tick. It can be greater than {@code round} due to misfire
     */
    long tick();

    /**
     * The execution round
     *
     * @return the execution round
     */
    long round();

    /**
     * Task result data per each round or latest result data when {@code isCompleted = true}
     *
     * @return task result data, might be null
     */
    @Nullable Object data();

    /**
     * Task result error per each round or latest result error when {@code isCompleted = true}
     *
     * @return task result error, might be null
     */
    @Nullable Throwable error();

    /**
     * Identify task executor is completed by cancel event or reach to limit round
     *
     * @return {@code true} if completed
     */
    boolean isCompleted();

    /**
     * Identify task execution is error or not
     *
     * @return {@code true} if error
     */
    default boolean isError() { return Objects.nonNull(error()); }

    /**
     * Identify task is reschedule or not
     *
     * @return {@code true} if reschedule
     */
    default boolean isReschedule() { return Objects.nonNull(rescheduledAt()) && round() > 0; }

}
