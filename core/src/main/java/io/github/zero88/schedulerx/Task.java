package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;

/**
 * Represents for Task to run on each trigger time.
 * <p/>
 * It is ideal if your concrete class is a class that has a constructor without argument, which makes it easier to init
 * a new task object in runtime via the configuration from an external system.
 *
 * @param <INPUT>  Type of task input data
 * @param <OUTPUT> Type of task result data
 * @since 1.0.0
 */
public interface Task<INPUT, OUTPUT> {

    /**
     * Identify task is async or not
     * <p>
     * If async task, then in execution time, task must use {@link ExecutionContext#complete(Object)}} or {@link
     * ExecutionContext#fail(Throwable)} when handling an async result
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
     *     <li>set value in case of task is success via {@link ExecutionContext#complete(Object)}</li>
     *     <li>set error in case of task is failed via {@link ExecutionContext#fail(Throwable)}</li>
     * </ul>
     *
     * @param jobData          job data
     * @param executionContext task execution context
     * @see JobData
     * @see ExecutionContext
     */
    void execute(@NotNull JobData<INPUT> jobData, @NotNull ExecutionContext<OUTPUT> executionContext);

}
