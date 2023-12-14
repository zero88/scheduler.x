package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.trigger.Trigger;
import io.vertx.core.WorkerExecutor;

/**
 * A scheduler schedules a job to run based on a particular trigger.
 *
 * @param <IN>      Type of Job input data
 * @param <OUT>     Type of Job result data
 * @param <TRIGGER> Type of Trigger
 * @apiNote This interface is renamed from {@code TriggerTaskExecutor} since {@code 2.0.0}
 * @see JobExecutor
 * @see Trigger
 * @since 2.0.0
 */
public interface Scheduler<IN, OUT, TRIGGER extends Trigger> extends JobExecutor<IN, OUT> {

    /**
     * Trigger type
     *
     * @return trigger
     */
    @NotNull TRIGGER trigger();

    /**
     * Start and run the {@code scheduler} in {@code Vertx worker thread pool}.
     */
    default void start() { start(null); }

    /**
     * Start and run the {@code scheduler} in a dedicated thread pool that is provided by a customized worker executor
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
