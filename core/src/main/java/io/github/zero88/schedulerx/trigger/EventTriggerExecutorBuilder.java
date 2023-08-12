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
 * Represents a builder that constructs {@link EventTriggerExecutor}
 *
 * @param <INPUT>  Type of Input data
 * @param <OUTPUT> Type of Result data
 * @since 2.0.0
 */
@VertxGen
public interface EventTriggerExecutorBuilder<INPUT, OUTPUT, T>
    // @formatter:off
    extends TriggerTaskExecutorBuilder<INPUT, OUTPUT, EventTrigger<T>,
                                          EventTriggerExecutor<INPUT, OUTPUT, T>, EventTriggerExecutorBuilder<INPUT, OUTPUT, T>> {
    // @formatter:on

    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull EventTrigger<T> trigger();

    @Fluent
    @NotNull EventTriggerExecutorBuilder<INPUT, OUTPUT, T> setVertx(@NotNull Vertx vertx);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull EventTriggerExecutorBuilder<INPUT, OUTPUT, T> setTask(@NotNull Task<INPUT, OUTPUT> task);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull EventTriggerExecutorBuilder<INPUT, OUTPUT, T> setTrigger(@NotNull EventTrigger<T> trigger);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull EventTriggerExecutorBuilder<INPUT, OUTPUT, T> setJobData(@NotNull JobData<INPUT> jobData);

    @Fluent
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull EventTriggerExecutorBuilder<INPUT, OUTPUT, T> setMonitor(@NotNull TaskExecutorMonitor<OUTPUT> monitor);

    @NotNull EventTriggerExecutor<INPUT, OUTPUT, T> build();

}
