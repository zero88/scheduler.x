package io.github.zero88.vertx.scheduler;

import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;

import lombok.NonNull;

/**
 * Represents for an executor run {@code task} in conditional loop
 *
 * @param <C> Type of TaskExecutionContext
 * @see TriggerTaskExecutor
 * @since 1.0.0
 */
public interface TaskExecutor<C extends TaskExecutionContext> {

    /**
     * Vertx
     *
     * @return vertx
     */
    @NonNull Vertx vertx();

    /**
     * Task executor state
     *
     * @return task executor state
     */
    @NonNull TaskExecutorState state();

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
    void executeTask(@NonNull C executionContext);

    /**
     * Cancel executor
     */
    void cancel();

}
