package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;

import io.vertx.core.Future;

/**
 * An interface supports reactive version for {@code Job}.
 * <p/>
 * This interface bridges you to write a new {@code Job} that based on your flavor async coding style and the reactive
 * library such as <a href="https://reactivex.io/">#reactivex</a> or
 * <a href="https://smallrye.io/smallrye-mutiny">#mutiny</a>
 *
 * @param <INPUT>  Type of job input
 * @param <OUTPUT> Type of job output
 * @param <R>      Type of {@code Rxified} execution result
 * @param <CTX>    Type of {@code Rxified} execution context
 * @since 2.0.0
 */
public interface FuturableJob<INPUT, OUTPUT, R, CTX> extends AsyncJob<INPUT, OUTPUT> {

    @Override
    default Future<OUTPUT> asyncExecute(@NotNull JobData<INPUT> jobData,
                                        @NotNull ExecutionContext<OUTPUT> executionContext) {
        return transformResult(doAsync(jobData, transformContext(executionContext)));
    }

    /**
     * Transform execution context to {@code Rxified} execution context
     *
     * @param executionContext job execution context
     * @return {@code Rxified} execution context
     * @see ExecutionContext
     */
    CTX transformContext(@NotNull ExecutionContext<OUTPUT> executionContext);

    /**
     * Transform the {@code Rxified} execution result to {@code Vert.x} {@link Future} version
     *
     * @param result {@code Rxified} execution result
     * @return the execution result in {@link Future}
     */
    Future<OUTPUT> transformResult(R result);

    /**
     * Async execute
     *
     * @param jobData          job data
     * @param executionContext job execution context
     * @return the {@code Rxified} execution result
     */
    R doAsync(@NotNull JobData<INPUT> jobData, @NotNull CTX executionContext);

}
