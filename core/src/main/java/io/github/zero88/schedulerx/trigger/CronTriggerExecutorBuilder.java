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
 * @param <INPUT> Type of Input data
 * @since 2.0.0
 */
@VertxGen
public interface CronTriggerExecutorBuilder<INPUT> extends
                                                   TriggerTaskExecutorBuilder<INPUT, CronTrigger,
                                                                                 CronTriggerExecutor<INPUT>,
                                                                                 CronTriggerExecutorBuilder<INPUT>> {

    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull CronTrigger trigger();

    @Fluent
    @NotNull CronTriggerExecutorBuilder<INPUT> setVertx(@NotNull Vertx vertx);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull CronTriggerExecutorBuilder<INPUT> setTask(@NotNull Task<INPUT> task);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull CronTriggerExecutorBuilder<INPUT> setTrigger(@NotNull CronTrigger trigger);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull CronTriggerExecutorBuilder<INPUT> setJobData(@NotNull JobData<INPUT> jobData);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull CronTriggerExecutorBuilder<INPUT> setMonitor(@NotNull TaskExecutorMonitor monitor);

    @NotNull CronTriggerExecutor<INPUT> build();

}
