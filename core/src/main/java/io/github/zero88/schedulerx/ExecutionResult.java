package io.github.zero88.schedulerx;

import java.time.Instant;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.trigger.TriggerContext;

/**
 * Represents for task result will be pass on each event of {@link SchedulingMonitor}
 *
 * @param <OUTPUT> Type of task result data
 * @apiNote This interface is renamed from {@code TaskResult} since {@code 2.0.0}
 * @see SchedulingMonitor
 * @since 2.0.0
 */
public interface ExecutionResult<OUTPUT> {

    /**
     * Get the unique id in external system that is declared in {@link JobData#externalId()}.
     *
     * @param <T> Type of external id
     * @return the external id, it can be nullable
     * @apiNote The type of the external id must be the same as your declaration in {@link JobData#externalId()}.
     *     Please use the correct data type otherwise you may get {@link ClassCastException} at runtime.
     */
    @Nullable <T> T externalId();

    TriggerContext triggerContext();

    /**
     * Only {@code not null} in {@link SchedulingMonitor#onUnableSchedule(ExecutionResult)}
     *
     * @return unschedule at time
     */
    Instant unscheduledAt();

    /**
     * Only {@code not null} if reschedule {@link SchedulingMonitor#onUnableSchedule(ExecutionResult)}
     *
     * @return reschedule at time
     * @see #isReschedule()
     */
    Instant rescheduledAt();

    /**
     * Task executor is available to run, in other words "task is scheduled at a time"
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
    @Nullable OUTPUT data();

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
