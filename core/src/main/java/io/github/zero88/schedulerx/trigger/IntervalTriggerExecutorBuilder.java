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
public interface IntervalTriggerExecutorBuilder
    extends TriggerTaskExecutorBuilder<IntervalTrigger, IntervalTriggerExecutor, IntervalTriggerExecutorBuilder> {

    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull IntervalTrigger trigger();

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull IntervalTriggerExecutorBuilder setVertx(@NotNull Vertx vertx);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull IntervalTriggerExecutorBuilder setTask(@NotNull Task task);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull IntervalTriggerExecutorBuilder setTrigger(@NotNull IntervalTrigger trigger);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull IntervalTriggerExecutorBuilder setJobData(@NotNull JobData jobData);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull IntervalTriggerExecutorBuilder setMonitor(@NotNull TaskExecutorMonitor monitor);

    @NotNull IntervalTriggerExecutor build();

}
