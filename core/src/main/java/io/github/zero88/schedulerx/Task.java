package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.trigger.Trigger;

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
    void execute(@NotNull JobData jobData, @NotNull TaskExecutionContext executionContext);

}
