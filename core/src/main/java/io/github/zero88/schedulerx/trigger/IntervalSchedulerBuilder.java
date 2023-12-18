package io.github.zero88.schedulerx.trigger;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.Job;
import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.SchedulerBuilder;
import io.github.zero88.schedulerx.SchedulingMonitor;
import io.github.zero88.schedulerx.TimeoutPolicy;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

/**
 * Represents a builder that constructs {@link IntervalScheduler}
 *
 * @since 2.0.0
 */
@VertxGen
public interface IntervalSchedulerBuilder<IN, OUT>
    extends SchedulerBuilder<IN, OUT, IntervalTrigger, IntervalScheduler<IN, OUT>, IntervalSchedulerBuilder<IN, OUT>> {

    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull IntervalTrigger trigger();

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull IntervalSchedulerBuilder<IN, OUT> setVertx(@NotNull Vertx vertx);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull IntervalSchedulerBuilder<IN, OUT> setTrigger(@NotNull IntervalTrigger trigger);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull IntervalSchedulerBuilder<IN, OUT> setMonitor(@NotNull SchedulingMonitor<OUT> monitor);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull IntervalSchedulerBuilder<IN, OUT> setJob(@NotNull Job<IN, OUT> job);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull IntervalSchedulerBuilder<IN, OUT> setJobData(@NotNull JobData<IN> jobData);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull IntervalSchedulerBuilder<IN, OUT> setTimeoutPolicy(@NotNull TimeoutPolicy timeoutPolicy);

    @NotNull IntervalScheduler<IN, OUT> build();

}
