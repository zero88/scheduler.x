package io.github.zero88.schedulerx.trigger;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.Scheduler;
import io.github.zero88.schedulerx.trigger.CronSchedulerImpl.CronSchedulerBuilderImpl;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;

/**
 * A timebase scheduler supports the cron-like scheduling.
 *
 * @see CronTrigger
 * @since 2.0.0
 */
@VertxGen
public interface CronScheduler extends Scheduler<CronTrigger> {

    static <IN, OUT> CronSchedulerBuilder<IN, OUT> builder() {
        return new CronSchedulerBuilderImpl<>();
    }

    @Override
    @NotNull Vertx vertx();

    @Override
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull CronTrigger trigger();

    @Override
    default void start() { start(null); }

    @Override
    void start(WorkerExecutor workerExecutor);

    @Override
    void cancel();

}
