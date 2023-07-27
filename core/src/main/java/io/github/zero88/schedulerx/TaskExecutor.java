package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.WorkerExecutor;

/**
 * Represents for an executor run {@code task} in conditional loop
 *
 * @since 1.0.0
 */
@VertxGen(concrete = false)
public interface TaskExecutor extends TaskExecutorProperties {

    /**
     * Task executor state
     *
     * @return task executor state
     */
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull TaskExecutorState state();

    /**
     * Start and run in {@code Vertx worker thread pool}
     */
    default void start() { start(null); }

    /**
     * Start and run in a dedicated thread pool that is provided by a customized worker executor
     *
     * @param workerExecutor worker executor
     * @see WorkerExecutor
     */
    void start(WorkerExecutor workerExecutor);

    /**
     * Execute task
     *
     * @param executionContext execution context
     */
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    void executeTask(@NotNull TaskExecutionContext executionContext);

    /**
     * Cancel executor
     */
    void cancel();

}
