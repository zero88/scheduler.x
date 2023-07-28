package io.github.zero88.schedulerx;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

/**
 * Shared immutable fields between TaskExecutor and its builder
 *
 * @param <INPUT> Type of input data
 * @since 2.0.0
 */
@Internal
@VertxGen(concrete = false)
public interface TaskExecutorProperties<INPUT> {

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
     * @see TaskExecutorMonitor
     */
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull TaskExecutorMonitor monitor();

    /**
     * Task to execute per round
     *
     * @return task
     * @see Task
     */
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull Task<INPUT> task();

    /**
     * Defines job data as input task data
     *
     * @return job data
     * @see JobData
     */
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull JobData<INPUT> jobData();

}
