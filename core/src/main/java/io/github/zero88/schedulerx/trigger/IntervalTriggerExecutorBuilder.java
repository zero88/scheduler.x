package io.github.zero88.schedulerx.trigger;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.Task;
import io.github.zero88.schedulerx.TaskExecutorMonitor;
import io.github.zero88.schedulerx.TriggerTaskExecutorBuilder;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

/**
 * Represents a builder that constructs {@link IntervalTriggerExecutor}
 *
 * @since 2.0.0
 */
@VertxGen
public interface IntervalTriggerExecutorBuilder<IN, OUT>
    // @formatter:off
    extends TriggerTaskExecutorBuilder<IN, OUT, IntervalTrigger,
                                          IntervalTriggerExecutor<IN, OUT>, IntervalTriggerExecutorBuilder<IN, OUT>> {
    // @formatter:on

    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull IntervalTrigger trigger();

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull IntervalTriggerExecutorBuilder<IN, OUT> setVertx(@NotNull Vertx vertx);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull IntervalTriggerExecutorBuilder<IN, OUT> setTask(@NotNull Task<IN, OUT> task);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull IntervalTriggerExecutorBuilder<IN, OUT> setTrigger(@NotNull IntervalTrigger trigger);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull IntervalTriggerExecutorBuilder<IN, OUT> setJobData(@NotNull JobData<IN> jobData);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull IntervalTriggerExecutorBuilder<IN, OUT> setMonitor(@NotNull TaskExecutorMonitor<OUT> monitor);

    @NotNull IntervalTriggerExecutor<IN, OUT> build();

}
