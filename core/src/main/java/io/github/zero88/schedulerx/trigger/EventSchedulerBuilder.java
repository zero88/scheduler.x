package io.github.zero88.schedulerx.trigger;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.Task;
import io.github.zero88.schedulerx.SchedulingMonitor;
import io.github.zero88.schedulerx.SchedulerBuilder;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

/**
 * Represents a builder that constructs {@link EventScheduler}
 *
 * @param <IN>  Type of task input data
 * @param <OUT> Type of task result data
 * @param <T>   Type of event message
 * @since 2.0.0
 */
@VertxGen
public interface EventSchedulerBuilder<IN, OUT, T>
    extends SchedulerBuilder<IN, OUT, EventTrigger<T>, EventScheduler<IN, OUT, T>, EventSchedulerBuilder<IN, OUT, T>> {

    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull EventTrigger<T> trigger();

    @Fluent
    @NotNull EventSchedulerBuilder<IN, OUT, T> setVertx(@NotNull Vertx vertx);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull EventSchedulerBuilder<IN, OUT, T> setTask(@NotNull Task<IN, OUT> task);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull EventSchedulerBuilder<IN, OUT, T> setTrigger(@NotNull EventTrigger<T> trigger);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull EventSchedulerBuilder<IN, OUT, T> setJobData(@NotNull JobData<IN> jobData);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull EventSchedulerBuilder<IN, OUT, T> setMonitor(@NotNull SchedulingMonitor<OUT> monitor);

    @NotNull EventScheduler<IN, OUT, T> build();

}
