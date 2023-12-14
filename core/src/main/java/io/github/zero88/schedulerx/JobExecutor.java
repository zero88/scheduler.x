package io.github.zero88.schedulerx;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;

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
     * Execute job
     *
     * @param executionContext execution context
     */
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    void executeJob(ExecutionContext<OUT> executionContext);

}
