package io.github.zero88.schedulerx;

import io.github.zero88.schedulerx.trigger.Trigger;
import io.vertx.core.WorkerExecutor;

/**
 * A scheduler schedules a job to run based on a particular trigger.
 *
 * @apiNote This interface is renamed from {@code TriggerTaskExecutor} since {@code 2.0.0}
 * @see IntervalScheduler
 * @see CronScheduler
 * @see EventScheduler
 * @since 2.0.0
 */
public interface Scheduler<TRIGGER extends Trigger> extends HasTrigger<TRIGGER>, HasVertx {

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
     * Cancel scheduler
     */
    void cancel();

}
