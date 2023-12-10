package io.github.zero88.schedulerx;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

/**
 * Shared immutable fields between TaskExecutor and its builder.
 * <p/>
 * This class is designed to internal usage, don't refer it in your code.
 *
 * @param <IN>  Type of task input data
 * @param <OUT> Type of task result data
 * @since 2.0.0
 */
@Internal
@VertxGen(concrete = false)
public interface TaskExecutorProperties<IN, OUT> {

    /**
     * Vertx
     *
     * @return vertx
     */
    @NotNull Vertx vertx();

    /**
     * Defines a task executor monitor
     *
     * @return task executor monitor
     * @see SchedulingMonitor
     */
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull SchedulingMonitor<OUT> monitor();

    /**
     * Task to execute per round
     *
     * @return task
     * @see Task
     */
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull Task<IN, OUT> task();

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
