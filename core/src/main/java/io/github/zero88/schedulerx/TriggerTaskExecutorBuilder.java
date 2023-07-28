package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.trigger.Trigger;
import io.vertx.core.Vertx;

/**
 * Represents for the high level of a builder that construct {@code TriggerTaskExecutor}
 *
 * @param <INPUT>    Type of job input data
 * @param <TRIGGER>  Type of Trigger
 * @param <EXECUTOR> Type of Trigger Task Executor
 * @param <SELF>     Type of Executor Builder
 * @see Trigger
 * @see TriggerTaskExecutor
 * @since 2.0.0
 */
public interface TriggerTaskExecutorBuilder<INPUT, TRIGGER extends Trigger,
                                               EXECUTOR extends TriggerTaskExecutor<INPUT, TRIGGER>,
                                               SELF extends TriggerTaskExecutorBuilder<INPUT, TRIGGER, EXECUTOR, SELF>>
    extends TaskExecutorProperties<INPUT> {

    @NotNull TRIGGER trigger();

    @NotNull SELF setVertx(@NotNull Vertx vertx);

    @NotNull SELF setTask(@NotNull Task<INPUT> task);

    @NotNull SELF setTrigger(@NotNull TRIGGER trigger);

    @NotNull SELF setJobData(@NotNull JobData<INPUT> jobData);

    @NotNull SELF setMonitor(@NotNull TaskExecutorMonitor monitor);

    @NotNull EXECUTOR build();

}
