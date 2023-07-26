package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.trigger.Trigger;
import io.vertx.core.Vertx;

/**
 * Represents for the high level of a builder that construct {@code TriggerTaskExecutor}
 *
 * @param <TRIGGER>  Type of Trigger
 * @param <EXECUTOR> Type of Trigger Task Executor
 * @param <SELF>     Type of Executor Builder
 * @see Trigger
 * @see TriggerTaskExecutor
 * @since 2.0.0
 */
public interface TriggerTaskExecutorBuilder<TRIGGER extends Trigger, EXECUTOR extends TriggerTaskExecutor<TRIGGER>,
                                               SELF extends TriggerTaskExecutorBuilder<TRIGGER, EXECUTOR, SELF>> {

    @NotNull SELF setVertx(@NotNull Vertx vertx);

    @NotNull SELF setTask(@NotNull Task task);

    @NotNull SELF setTrigger(@NotNull TRIGGER trigger);

    @NotNull SELF setJobData(@NotNull JobData jobData);

    @NotNull SELF setMonitor(@NotNull TaskExecutorMonitor monitor);

    @NotNull Vertx vertx();

    @NotNull Task task();

    @NotNull TRIGGER trigger();

    @NotNull JobData jobData();

    @NotNull TaskExecutorMonitor monitor();

    @NotNull EXECUTOR build();

}
