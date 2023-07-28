package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;

/**
 * Represents for a monitor that watches lifecycle event in executor.
 * <p/>
 * It can be used to persist or distribute the task result per each round.
 *
 * @param <OUTPUT> Type of Result data
 * @see TaskResult
 * @since 1.0.0
 */
public interface TaskExecutorMonitor<OUTPUT> {

    /**
     * Invoke when executor is unable to schedule
     *
     * @param result task result
     * @see TaskResult
     */
    void onUnableSchedule(@NotNull TaskResult<OUTPUT> result);

    /**
     * Invoke after executor is scheduled or rescheduled
     *
     * @param result task result
     */
    void onSchedule(@NotNull TaskResult<OUTPUT> result);

    /**
     * Invoke when misfire the execution, one reason is due to task is still running when trigger a new round execution
     *
     * @param result task result
     */
    void onMisfire(@NotNull TaskResult<OUTPUT> result);

    /**
     * Invoke after each round is finished regardless a round execution is success or fail
     *
     * @param result task result
     */
    void onEach(@NotNull TaskResult<OUTPUT> result);

    /**
     * Invoke after executor is completed
     *
     * @param result task result
     */
    void onCompleted(@NotNull TaskResult<OUTPUT> result);

}
