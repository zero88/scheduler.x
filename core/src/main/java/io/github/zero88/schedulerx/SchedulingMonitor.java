package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.trigger.Trigger;

/**
 * A monitor watches lifecycle events in the scheduler, which is used to distribute the execution result per each
 * scheduled fire time.
 *
 * @param <OUT> Type of job result data
 * @apiNote This interface is renamed from {@code TaskExecutorMonitor} since {@code 2.0.0}
 * @see ExecutionResult
 * @since 2.0.0
 */
public interface SchedulingMonitor<OUT> {

    /**
     * This method is invoked when the executor is unable to schedule due to the trigger has invalid configuration that
     * is verified by {@link Trigger#validate()}.
     *
     * @param result the execution result
     * @see ExecutionResult
     */
    void onUnableSchedule(@NotNull ExecutionResult<OUT> result);

    /**
     * This method is invoked when the executor is scheduled or rescheduled.
     *
     * @param result the execution result
     */
    void onSchedule(@NotNull ExecutionResult<OUT> result);

    /**
     * This method is invoked when the executor misfires the execution.
     * <p/>
     * one reason is due to job is still running when trigger a new round execution
     *
     * @param result the execution result
     */
    void onMisfire(@NotNull ExecutionResult<OUT> result);

    /**
     * This method is invoked when the executor finishes each execution round regardless an execution result is success
     * or fail.
     *
     * @param result the execution result
     */
    void onEach(@NotNull ExecutionResult<OUT> result);

    /**
     * This method is invoked when the executor is completed, means no execution round is executed.
     *
     * @param result the execution result
     */
    void onCompleted(@NotNull ExecutionResult<OUT> result);

}
