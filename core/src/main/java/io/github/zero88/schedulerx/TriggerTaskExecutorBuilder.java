package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.trigger.Trigger;
import io.vertx.core.Vertx;

/**
 * @param <TRIGGER>
 * @param <CTX>
 * @param <EXECUTOR>
 * @param <SELF>
 * @since 2.0.0
 */
public interface TriggerTaskExecutorBuilder<TRIGGER extends Trigger, CTX extends TaskExecutionContext,
                                               EXECUTOR extends TriggerTaskExecutor<TRIGGER, CTX>,
                                               SELF extends TriggerTaskExecutorBuilder<TRIGGER, CTX, EXECUTOR, SELF>> {

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
