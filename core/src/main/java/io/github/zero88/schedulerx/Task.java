package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;

/**
 * Represents for Task to run on each trigger time
 *
 * @param <T> Type of input data
 * @since 1.0.0
 */
public interface Task<T> {

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
     * Execute task.
     * <p/>
     * After executed task, please remember to:
     * <ul>
     *     <li>set value in case of task is success via {@link TaskExecutionContext#complete(Object)}</li>
     *     <li>set error in case of task is failed via {@link TaskExecutionContext#fail(Throwable)}</li>
     * </ul>
     *
     * @param jobData          job data
     * @param executionContext task execution context
     * @see JobData
     * @see TaskExecutionContext
     */
    void execute(@NotNull JobData<T> jobData, @NotNull TaskExecutionContext executionContext);

}
