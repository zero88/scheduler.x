package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;

import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * Represents for Async Job to run on each trigger time.
 * <p/>
 * It is ideal if your concrete class is a class that has a constructor without argument, which makes it easier to init
 * a new job object in runtime via the configuration from an external system.
 *
 * @since 2.0.0
 */
public interface AsyncJob<INPUT, OUTPUT> extends Job<INPUT, OUTPUT> {

    /**
     * Do {@link #asyncExecute(JobData, ExecutionContext)} internally, then handling:
     * <ul>
     *     <li>an async success result by {@link Future#onSuccess(Handler)} with the handler
     *     {@link ExecutionContext#complete(Object)}}</li>
     *     <li>an async failure result by {@link Future#onSuccess(Handler)} with the handler
     *      {@link ExecutionContext#fail(Throwable)}</li>
     * </ul>
     *
     * @param jobData          job data
     * @param executionContext job execution context
     * @see JobData
     * @see ExecutionContext
     */
    @Override
    default void execute(@NotNull JobData<INPUT> jobData, @NotNull ExecutionContext<OUTPUT> executionContext) {
        asyncExecute(jobData, executionContext).onSuccess(executionContext::complete).onFailure(executionContext::fail);
    }

    /**
     * Async execute job
     * <p>
     * <em><strong>WARNING</strong></em>: After execution, be aware to call a terminal operation of {@link Future} such
     * as {@link Future#onSuccess(Handler)}, {@link Future#onFailure(Handler)} or {@link Future#onComplete(Handler)}.
     * The async job is already registered these handlers, if several {@code handler}s are registered, there is no
     * guarantee that they will be invoked in order of registration.
     *
     * @param jobData          job data
     * @param executionContext job execution context
     * @return the job result in future
     * @see JobData
     * @see ExecutionContext
     */
    Future<OUTPUT> asyncExecute(@NotNull JobData<INPUT> jobData, @NotNull ExecutionContext<OUTPUT> executionContext);

}
