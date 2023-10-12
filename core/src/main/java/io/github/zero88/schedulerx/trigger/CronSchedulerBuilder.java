package io.github.zero88.schedulerx.trigger;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.SchedulerBuilder;
import io.github.zero88.schedulerx.SchedulingMonitor;
import io.github.zero88.schedulerx.Task;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

/**
 * Represents a builder that constructs {@link CronScheduler}
 *
 * @param <IN>  Type of task input data
 * @param <OUT> Type of task result data
 * @since 2.0.0
 */
@VertxGen
public interface CronSchedulerBuilder<IN, OUT>
    extends SchedulerBuilder<IN, OUT, CronTrigger, CronScheduler<IN, OUT>, CronSchedulerBuilder<IN, OUT>> {

    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull CronTrigger trigger();

    @Fluent
    @NotNull CronSchedulerBuilder<IN, OUT> setVertx(@NotNull Vertx vertx);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull CronSchedulerBuilder<IN, OUT> setTask(@NotNull Task<IN, OUT> task);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull CronSchedulerBuilder<IN, OUT> setTrigger(@NotNull CronTrigger trigger);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull CronSchedulerBuilder<IN, OUT> setJobData(@NotNull JobData<IN> jobData);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull CronSchedulerBuilder<IN, OUT> setMonitor(@NotNull SchedulingMonitor<OUT> monitor);

    @NotNull CronScheduler<IN, OUT> build();

}
