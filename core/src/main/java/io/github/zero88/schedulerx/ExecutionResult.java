package io.github.zero88.schedulerx;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.trigger.TriggerContext;

/**
 * Represents for an execution result will be pass on each event of {@link SchedulingMonitor}
 *
 * @param <OUTPUT> Type of execution result
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

    /**
     * Identify the current trigger information in the execution runtime.
     *
     * @return the trigger context
     */
    TriggerContext triggerContext();

    /**
     * Identify the trigger that cannot be scheduled on the system timer at a clock time.
     *
     * @return unschedule at time
     * @see SchedulingMonitor#onUnableSchedule(ExecutionResult)
     */
    Instant unscheduledAt();

    /**
     * Identify the trigger is registered on the system timer at a clock time.
     *
     * @return available at time
     * @see SchedulingMonitor#onSchedule(ExecutionResult)
     */
    Instant availableAt();

    /**
     * Identify the trigger is re-scheduled on the system timer at a clock time, only in case of trigger type is
     * {@code cron}.
     *
     * @return rescheduled at time
     * @see SchedulingMonitor#onSchedule(ExecutionResult)
     */
    Instant rescheduledAt();

    /**
     * Identify the system timer fires the trigger at a clock time.
     *
     * @return fired at time
     * @see SchedulingMonitor#onEach(ExecutionResult)
     * @see SchedulingMonitor#onMisfire(ExecutionResult)
     */
    Instant firedAt();

    /**
     * Identify the trigger is ready to run new execution round at a clock time.
     *
     * @return triggered at time
     * @see SchedulingMonitor#onEach(ExecutionResult)
     */
    Instant triggeredAt();

    /**
     * Identify the trigger job that is started to execute at a clock time.
     *
     * @return executed at time
     * @see SchedulingMonitor#onEach(ExecutionResult)
     */
    Instant executedAt();

    /**
     * Identify the trigger tick time is already processed at a clock time, regardless its status is misfire or its job
     * is executed.
     *
     * @return finished at time
     * @see SchedulingMonitor#onEach(ExecutionResult)
     * @see SchedulingMonitor#onMisfire(ExecutionResult)
     */
    Instant finishedAt();

    /**
     * Identify the trigger is completed at a clock time, that means no more is fired by the system timer.
     *
     * @return completed at time
     * @see SchedulingMonitor#onCompleted(ExecutionResult)
     */
    Instant completedAt();

    /**
     * The current number of times that the system timer fires the trigger. This value can be greater than
     * {@link #round()} due to misfire.
     *
     * @return the tick
     * @apiNote The time at which the number of rounds is changed is {@link #firedAt()}
     */
    long tick();

    /**
     * The current number of times that the trigger's job is executed.
     *
     * @return the round
     * @apiNote The time at which the number of rounds is changed is {@link #triggeredAt()}
     */
    long round();

    /**
     * Job result data per each round or latest result data if trigger is completed.
     *
     * @return job result data, might be null
     */
    @Nullable OUTPUT data();

    /**
     * Job result error per each round or latest result error if trigger is completed.
     *
     * @return job result error, might be null
     */
    @Nullable Throwable error();

    /**
     * Identify Job execution is error or not
     *
     * @return {@code true} if error
     */
    default boolean isError() { return Objects.nonNull(error()); }

    /**
     * Identify Job execution is timed out
     *
     * @return {@code true} if timeout error
     */
    default boolean isTimeout() { return error() instanceof TimeoutException; }

    /**
     * Check whether the trigger is re-registered in the system timer or not after the trigger is available, only in
     * case of trigger type is {@code cron}.
     *
     * @return {@code true} if reschedule
     * @see #rescheduledAt()
     */
    default boolean isReschedule() { return Objects.nonNull(rescheduledAt()) && tick() > 0; }

}
