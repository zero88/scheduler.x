package io.github.zero88.schedulerx.trigger;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.Scheduler;
import io.github.zero88.schedulerx.trigger.IntervalSchedulerImpl.IntervalSchedulerBuilderImpl;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;

/**
 * A timebase scheduler supports the interval schedules.
 *
 * @param <IN>  Type of job input data
 * @param <OUT> Type of job result data
 * @see IntervalTrigger
 * @since 2.0.0
 */
@VertxGen
public interface IntervalScheduler<IN, OUT> extends Scheduler<IN, OUT, IntervalTrigger> {

    static <IN, OUT> IntervalSchedulerBuilder<IN, OUT> builder() {
        return new IntervalSchedulerBuilderImpl<>();
    }

    @Override
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull IntervalTrigger trigger();

}
