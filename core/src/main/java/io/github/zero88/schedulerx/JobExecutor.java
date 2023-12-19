package io.github.zero88.schedulerx;

import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * Represents for an executor run a {@code job} in conditional loop.
 *
 * @param <OUT> Type of job result data
 * @apiNote This interface is renamed from {@code TaskExecutor} since {@code 2.0.0}
 * @since 1.0.0
 */
@Internal
@FunctionalInterface
public interface JobExecutor<OUT> {

    /**
     * Execute job
     *
     * @param executionContext execution context
     */
    void executeJob(ExecutionContext<OUT> executionContext);

}
