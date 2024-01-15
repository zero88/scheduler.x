package io.github.zero88.schedulerx;

import java.time.Duration;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;

/**
 * A factory to create {@link WorkerExecutor} based on the execution timeout policy.
 *
 * @since 2.0.0
 */
@Internal
public interface WorkerExecutorFactory {

    static @NotNull WorkerExecutor createExecutionWorker(@NotNull Vertx vertx, @NotNull TimeoutPolicy timeoutPolicy) {
        return create(vertx, timeoutPolicy.executionTimeout(), DefaultOptions.getInstance().executionThreadPrefix,
                      DefaultOptions.getInstance().executionThreadPoolSize);
    }

    static @NotNull WorkerExecutor createMonitorWorker(@NotNull Vertx vertx) {
        return create(vertx, DefaultOptions.getInstance().monitorMaxTimeout,
                      DefaultOptions.getInstance().monitorThreadPrefix,
                      DefaultOptions.getInstance().monitorThreadPoolSize);
    }

    private static @NotNull WorkerExecutor create(@NotNull Vertx vertx, Duration timeout, String threadPrefix,
                                                  int poolSize) {
        final String threadName = genThreadName(threadPrefix, timeout);
        return vertx.createSharedWorkerExecutor(threadName, poolSize, timeout.toMillis());
    }

    private static String genThreadName(@NotNull String threadPrefix, @NotNull Duration timeout) {
        return threadPrefix + "-" + timeout.getSeconds() + "s";
    }

}
