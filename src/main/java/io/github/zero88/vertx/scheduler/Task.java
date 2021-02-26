package io.github.zero88.vertx.scheduler;

import io.github.zero88.vertx.scheduler.trigger.Trigger;

import lombok.NonNull;

/**
 * Represents for Task to run on each trigger time
 *
 * @see Trigger
 * @since 1.0.0
 */
public interface Task {

    /**
     * Identify task is async or not
     * <p>
     * If async task, then in execution time, task must use {@link TaskExecutionContext#complete(Object)}} or {@link
     * TaskExecutionContext#fail(Throwable)} when handling an async result
     *
     * @return true if it is async task
     */
    default boolean isAsync() {
        return false;
    }

    /**
     * Execute task
     *
     * @param jobData          job data
     * @param executionContext task execution context
     * @see TaskExecutionContext
     */
    void execute(@NonNull JobData jobData, @NonNull TaskExecutionContext executionContext);

}
