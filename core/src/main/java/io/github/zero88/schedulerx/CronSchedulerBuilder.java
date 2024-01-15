package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.trigger.CronTrigger;
import io.github.zero88.schedulerx.trigger.TriggerEvaluator;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

/**
 * Represents a builder that constructs {@link CronScheduler}
 *
 * @param <IN>  Type of job input data
 * @param <OUT> Type of job result data
 * @since 2.0.0
 */
@VertxGen
public interface CronSchedulerBuilder<IN, OUT>
    extends SchedulerBuilder<IN, OUT, CronTrigger, CronScheduler, CronSchedulerBuilder<IN, OUT>> {

    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull CronTrigger trigger();

    @Fluent
    @NotNull CronSchedulerBuilder<IN, OUT> setVertx(@NotNull Vertx vertx);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull CronSchedulerBuilder<IN, OUT> setTrigger(@NotNull CronTrigger trigger);

    @Override
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull CronSchedulerBuilder<IN, OUT> setTriggerEvaluator(@NotNull TriggerEvaluator evaluator);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull CronSchedulerBuilder<IN, OUT> setMonitor(@NotNull SchedulingMonitor<OUT> monitor);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull CronSchedulerBuilder<IN, OUT> setJob(@NotNull Job<IN, OUT> job);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull CronSchedulerBuilder<IN, OUT> setJobData(@NotNull JobData<IN> jobData);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull CronSchedulerBuilder<IN, OUT> setTimeoutPolicy(@NotNull TimeoutPolicy timeoutPolicy);

    @NotNull CronScheduler build();

}
