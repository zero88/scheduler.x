package io.github.zero88.schedulerx;

import java.time.Duration;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;

/**
 * A factory to create {@link WorkerExecutor}.
 *
 * @since 2.0.0
 */
@Internal
public interface WorkerExecutorFactory {

    /**
     * Create a worker executor on which the scheduling execution operation runs.
     * <p/>
     * <em><strong>Note</strong></em>:
     * <ul>
     *     <li>The thread-name prefix is retrieved from {@link DefaultOptions#executionThreadPrefix}</li>
     *     <li>The thread pool size is retrieved from {@link DefaultOptions#executionThreadPoolSize}</li>
     * </ul>
     *
     * @param vertx         Vert.x
     * @param timeoutPolicy the timeout policy
     * @return new instance of worker executor
     */
    static @NotNull WorkerExecutor createExecutionWorker(@NotNull Vertx vertx, @NotNull TimeoutPolicy timeoutPolicy) {
        return create(vertx, timeoutPolicy.executionTimeout(), DefaultOptions.getInstance().executionThreadPrefix,
                      DefaultOptions.getInstance().executionThreadPoolSize);
    }

    /**
     * Create a worker executor on which the scheduling monitor operation runs.
     * <p/>
     * <em><strong>Note</strong></em>:
     * <ul>
     *     <li>The timeout is retrieved from {@link DefaultOptions#monitorMaxTimeout}</li>
     *     <li>The thread-name prefix is retrieved from {@link DefaultOptions#monitorThreadPrefix}</li>
     *     <li>The thread pool size is retrieved from {@link DefaultOptions#monitorThreadPoolSize}</li>
     * </ul>
     *
     * @param vertx Vert.x
     * @return new instance of worker executor
     */
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
