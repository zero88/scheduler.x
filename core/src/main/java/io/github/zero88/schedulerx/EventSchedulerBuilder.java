package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.trigger.EventTrigger;
import io.github.zero88.schedulerx.trigger.TriggerEvaluator;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

/**
 * Represents a builder that constructs {@link EventScheduler}
 *
 * @param <IN>  Type of job input data
 * @param <OUT> Type of job result data
 * @param <T>   Type of event message
 * @since 2.0.0
 */
@VertxGen
public interface EventSchedulerBuilder<IN, OUT, T>
    extends SchedulerBuilder<IN, OUT, EventTrigger<T>, EventScheduler<T>, EventSchedulerBuilder<IN, OUT, T>> {

    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull EventTrigger<T> trigger();

    @Fluent
    @NotNull EventSchedulerBuilder<IN, OUT, T> setVertx(@NotNull Vertx vertx);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull EventSchedulerBuilder<IN, OUT, T> setTrigger(@NotNull EventTrigger<T> trigger);

    @Override
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull EventSchedulerBuilder<IN, OUT, T> setTriggerEvaluator(@NotNull TriggerEvaluator evaluator);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull EventSchedulerBuilder<IN, OUT, T> setMonitor(@NotNull SchedulingMonitor<OUT> monitor);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull EventSchedulerBuilder<IN, OUT, T> setJob(@NotNull Job<IN, OUT> job);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull EventSchedulerBuilder<IN, OUT, T> setJobData(@NotNull JobData<IN> jobData);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull EventSchedulerBuilder<IN, OUT, T> setTimeoutPolicy(@NotNull TimeoutPolicy timeoutPolicy);

    @NotNull EventScheduler<T> build();

}
