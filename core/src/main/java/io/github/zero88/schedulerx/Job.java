package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;

import io.vertx.core.Future;

/**
 * Represents for Job to run on each trigger time.
 * <p/>
 * It is ideal if your concrete class is a class that has a constructor without argument, which makes it easier to init
 * a new job object in runtime via the configuration from an external system.
 * <p/>
 * If you want to use the job that returns Vertx {@link Future}, please using {@link AsyncJob} for more intuitive API.
 *
 * @param <INPUT>  Type of job input data
 * @param <OUTPUT> Type of job result data
 * @apiNote This interface is renamed from {@code Task} since {@code 2.0.0}
 * @since 1.0.0
 */
public interface Job<INPUT, OUTPUT> {

    /**
     * Execute job.
     * <p/>
     * After executed job, please remember to:
     * <ul>
     *     <li>set value in case of job is success via {@link ExecutionContext#complete(Object)}</li>
     *     <li>set error in case of job is failed via {@link ExecutionContext#fail(Throwable)}</li>
     * </ul>
     *
     * @param jobData          job data
     * @param executionContext job execution context
     * @see JobData
     * @see ExecutionContext
     */
    void execute(@NotNull JobData<INPUT> jobData, @NotNull ExecutionContext<OUTPUT> executionContext);

}
