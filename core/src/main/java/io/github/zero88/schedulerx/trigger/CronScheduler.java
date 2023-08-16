package io.github.zero88.schedulerx.trigger;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.Scheduler;
import io.github.zero88.schedulerx.trigger.CronSchedulerImpl.CronSchedulerBuilderImpl;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;

/**
 * A timebase scheduler supports the cron-like scheduling.
 *
 * @param <IN>  Type of job input data
 * @param <OUT> Type of task result data
 * @see CronTrigger
 * @since 2.0.0
 */
@VertxGen
public interface CronScheduler<IN, OUT> extends Scheduler<IN, OUT, CronTrigger> {

    static <IN, OUT> CronSchedulerBuilder<IN, OUT> builder() {
        return new CronSchedulerBuilderImpl<>();
    }

    @Override
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull CronTrigger trigger();

}
