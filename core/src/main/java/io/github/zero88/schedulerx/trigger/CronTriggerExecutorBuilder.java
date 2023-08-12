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
 * @param <INPUT>  Type of Input data
 * @param <OUTPUT> Type of Result data
 * @since 2.0.0
 */
@VertxGen
public interface CronTriggerExecutorBuilder<INPUT, OUTPUT>
    // @formatter:off
    extends TriggerTaskExecutorBuilder<INPUT, OUTPUT, CronTrigger,
                                          CronTriggerExecutor<INPUT, OUTPUT>, CronTriggerExecutorBuilder<INPUT, OUTPUT>> {
    // @formatter:on

    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull CronTrigger trigger();

    @Fluent
    @NotNull CronTriggerExecutorBuilder<INPUT, OUTPUT> setVertx(@NotNull Vertx vertx);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull CronTriggerExecutorBuilder<INPUT, OUTPUT> setTask(@NotNull Task<INPUT, OUTPUT> task);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull CronTriggerExecutorBuilder<INPUT, OUTPUT> setTrigger(@NotNull CronTrigger trigger);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull CronTriggerExecutorBuilder<INPUT, OUTPUT> setJobData(@NotNull JobData<INPUT> jobData);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull CronTriggerExecutorBuilder<INPUT, OUTPUT> setMonitor(@NotNull TaskExecutorMonitor<OUTPUT> monitor);

    @NotNull CronTriggerExecutor<INPUT, OUTPUT> build();

}
