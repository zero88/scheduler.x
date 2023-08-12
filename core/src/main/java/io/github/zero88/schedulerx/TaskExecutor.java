package io.github.zero88.schedulerx;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.WorkerExecutor;

/**
 * Represents for an executor run a {@code task} in conditional loop.
 *
 * @param <INPUT>  Type of input data
 * @param <OUTPUT> Type of Result data
 * @since 1.0.0
 */
@VertxGen(concrete = false)
public interface TaskExecutor<INPUT, OUTPUT> extends TaskExecutorProperties<INPUT, OUTPUT> {

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
     * Cancel executor
     */
    void cancel();

}
