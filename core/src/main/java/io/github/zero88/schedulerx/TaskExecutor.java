package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;

import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;

/**
 * Represents for an executor run {@code task} in conditional loop
 *
 * @see TriggerTaskExecutor
 * @since 1.0.0
 */
public interface TaskExecutor {

    /**
     * Vertx
     *
     * @return vertx
     */
    @NotNull Vertx vertx();

    /**
     * Task executor state
     *
     * @return task executor state
     */
    @NotNull TaskExecutorState state();

    /**
     * Start and run in {@code Vertx worker thread pool}
     */
    default void start() {
        start(null);
    }

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
    void executeTask(@NotNull TaskExecutionContext executionContext);

    /**
     * Cancel executor
     */
    void cancel();

}
