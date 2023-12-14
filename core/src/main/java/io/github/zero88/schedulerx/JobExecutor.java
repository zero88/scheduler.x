package io.github.zero88.schedulerx;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.WorkerExecutor;

/**
 * Represents for an executor run a {@code job} in conditional loop.
 *
 * @param <IN>  Type of job input data
 * @param <OUT> Type of job result data
 * @apiNote This interface is renamed from {@code TaskExecutor} since {@code 2.0.0}
 * @since 1.0.0
 */
@VertxGen(concrete = false)
public interface JobExecutor<IN, OUT> extends JobExecutorProperties<IN, OUT> {

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
