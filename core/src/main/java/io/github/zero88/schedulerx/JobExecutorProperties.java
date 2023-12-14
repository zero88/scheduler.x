package io.github.zero88.schedulerx;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

/**
 * Shared immutable fields between JobExecutor and its builder.
 * <p/>
 * This class is designed to internal usage, don't refer it in your code.
 *
 * @param <IN>  Type of job input data
 * @param <OUT> Type of job result data
 * @since 2.0.0
 */
@Internal
@VertxGen(concrete = false)
public interface JobExecutorProperties<IN, OUT> {

    /**
     * Vertx
     *
     * @return vertx
     */
    @NotNull Vertx vertx();

    /**
     * Defines a job executor monitor
     *
     * @return job executor monitor
     * @see SchedulingMonitor
     */
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull SchedulingMonitor<OUT> monitor();

    /**
     * Job to execute per round
     *
     * @return job
     * @see Job
     */
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull Job<IN, OUT> job();

    /**
     * Declares the job input data
     *
     * @return job data
     * @see JobData
     */
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull JobData<IN> jobData();

    /**
     * Declares the timeout policy
     *
     * @return timeout policy
     * @see TimeoutPolicy
     */
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull TimeoutPolicy timeoutPolicy();

}
