package io.github.zero88.schedulerx;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

/**
 * Shared immutable fields between {@code JobExecutor} and its builder.
 * <p/>
 * This class is designed to internal usage, don't refer it in your code.
 *
 * @param <IN>  Type of job input data
 * @param <OUT> Type of job result data
 * @since 2.0.0
 */
@Internal
interface JobExecutorContext<IN, OUT> {

    /**
     * Job to execute per round
     *
     * @return job
     * @see Job
     */
    @NotNull Job<IN, OUT> job();

    /**
     * Declares the job input data
     *
     * @return job data
     * @see JobData
     */
    @NotNull JobData<IN> jobData();

    /**
     * Declares the timeout policy
     *
     * @return timeout policy
     * @see TimeoutPolicy
     */
    @NotNull TimeoutPolicy timeoutPolicy();

}
