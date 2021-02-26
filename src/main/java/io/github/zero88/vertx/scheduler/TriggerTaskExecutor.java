package io.github.zero88.vertx.scheduler;

import io.github.zero88.vertx.scheduler.trigger.Trigger;

import lombok.NonNull;

/**
 * Represents for an executor run task based on particular trigger
 *
 * @param <T> Type of Trigger
 * @param <C> Type of Task Execution context
 * @see Trigger
 * @see TaskExecutionContext
 * @since 1.0.0
 */
public interface TriggerTaskExecutor<T extends Trigger, C extends TaskExecutionContext> extends TaskExecutor<C> {

    /**
     * Task to execute per round
     *
     * @return task
     * @see Task
     */
    @NonNull Task task();

    /**
     * Trigger type
     *
     * @return trigger
     */
    @NonNull T trigger();

    /**
     * Defines job data as input task data
     *
     * @return job data
     * @see JobData
     */
    @NonNull JobData jobData();

    /**
     * Defines a task executor monitor
     *
     * @return task executor monitor
     * @see TaskExecutorMonitor
     */
    @NonNull TaskExecutorMonitor monitor();

}
