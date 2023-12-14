package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.impl.Utils;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.WorkerExecutor;

/**
 * A factory to create {@link WorkerExecutor} based on the execution timeout policy.
 *
 * @since 2.0.0
 */
public interface WorkerExecutorFactory {

    static @Nullable WorkerExecutor create(@NotNull Vertx vertx, @NotNull TimeoutPolicy timeoutPolicy) {
        return WorkerExecutorFactory.create(vertx, timeoutPolicy,
                                            DefaultOptions.getInstance().workerThreadPrefix + "-" +
                                            Utils.randomPositiveInt());
    }

    static @Nullable WorkerExecutor create(@NotNull Vertx vertx, @NotNull TimeoutPolicy timeoutPolicy,
                                           @Nullable String workerThreadName) {
        final long executionTimeout = timeoutPolicy.executionTimeout().toMillis();
        if (executionTimeout != VertxOptions.DEFAULT_MAX_WORKER_EXECUTE_TIME) {
            return vertx.createSharedWorkerExecutor(workerThreadName, DefaultOptions.getInstance().workerThreadPoolSize,
                                                    executionTimeout);
        }
        return null;
    }

}
