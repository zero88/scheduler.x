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
 * Represents a builder that constructs {@link CronTriggerExecutor}
 *
 * @since 2.0.0
 */
@VertxGen
public interface CronTriggerExecutorBuilder
    extends TriggerTaskExecutorBuilder<CronTrigger, CronTriggerExecutor, CronTriggerExecutorBuilder> {

    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull CronTrigger trigger();

    @Fluent
    @NotNull CronTriggerExecutorBuilder setVertx(@NotNull Vertx vertx);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull CronTriggerExecutorBuilder setTask(@NotNull Task task);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull CronTriggerExecutorBuilder setTrigger(@NotNull CronTrigger trigger);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull CronTriggerExecutorBuilder setJobData(@NotNull JobData jobData);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull CronTriggerExecutorBuilder setMonitor(@NotNull TaskExecutorMonitor monitor);

    @NotNull CronTriggerExecutor build();

}
