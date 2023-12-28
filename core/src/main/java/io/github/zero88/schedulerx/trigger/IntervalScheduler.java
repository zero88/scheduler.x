package io.github.zero88.schedulerx.trigger;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.Scheduler;
import io.github.zero88.schedulerx.trigger.IntervalSchedulerImpl.IntervalSchedulerBuilderImpl;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;

/**
 * A timebase scheduler supports the interval schedules.
 *
 * @see IntervalTrigger
 * @since 2.0.0
 */
@VertxGen
public interface IntervalScheduler extends Scheduler<IntervalTrigger> {

    static <IN, OUT> IntervalSchedulerBuilder<IN, OUT> builder() {
        return new IntervalSchedulerBuilderImpl<>();
    }

    @Override
    @NotNull Vertx vertx();

    @Override
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull IntervalTrigger trigger();

    @Override
    default void start() { start(null); }

    @Override
    void start(WorkerExecutor workerExecutor);

    @Override
    void cancel();

}
