package io.github.zero88.vertx.scheduler;

import lombok.NonNull;

/**
 * Represents for monitor that watches lifecycle event in executor
 *
 * @see TaskResult
 * @since 1.0.0
 */
public interface TaskExecutorMonitor {

    /**
     * Invoke when executor is unable to schedule
     *
     * @param result task result
     * @see TaskResult
     */
    void onUnableSchedule(@NonNull TaskResult result);

    /**
     * Invoke after executor is scheduled or rescheduled
     *
     * @param result task result
     */
    void onSchedule(@NonNull TaskResult result);

    /**
     * Invoke when misfire the execution, one reason is due to task is still running when trigger a new round execution
     *
     * @param result task result
     */
    void onMisfire(@NonNull TaskResult result);

    /**
     * Invoke after each round is finished regardless a round execution is success or fail
     *
     * @param result task result
     */
    void onEach(@NonNull TaskResult result);

    /**
     * Invoke after executor is completed
     *
     * @param result task result
     */
    void onCompleted(@NonNull TaskResult result);

}
