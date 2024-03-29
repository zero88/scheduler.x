package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.trigger.IntervalTrigger;
import io.github.zero88.schedulerx.trigger.TriggerEvaluator;
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
    extends SchedulerBuilder<IN, OUT, IntervalTrigger, IntervalScheduler, IntervalSchedulerBuilder<IN, OUT>> {

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull IntervalSchedulerBuilder<IN, OUT> setVertx(@NotNull Vertx vertx);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull IntervalSchedulerBuilder<IN, OUT> setTrigger(@NotNull IntervalTrigger trigger);

    @Override
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull IntervalSchedulerBuilder<IN, OUT> setTriggerEvaluator(@NotNull TriggerEvaluator evaluator);

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

    @NotNull IntervalScheduler build();

}
