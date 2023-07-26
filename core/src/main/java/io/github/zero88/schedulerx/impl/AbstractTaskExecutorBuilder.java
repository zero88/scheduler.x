package io.github.zero88.schedulerx.impl;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.Task;
import io.github.zero88.schedulerx.TaskExecutorLogMonitor;
import io.github.zero88.schedulerx.TaskExecutorMonitor;
import io.github.zero88.schedulerx.TriggerTaskExecutor;
import io.github.zero88.schedulerx.TriggerTaskExecutorBuilder;
import io.github.zero88.schedulerx.trigger.Trigger;
import io.vertx.core.Vertx;

/**
 * The base task executor builder
 */
@SuppressWarnings("unchecked")
public abstract class AbstractTaskExecutorBuilder<T extends Trigger, E extends TriggerTaskExecutor<T>,
                                                     B extends AbstractTaskExecutorBuilder<T, E, B>>
    implements TriggerTaskExecutorBuilder<T, E, B> {

    private Vertx vertx;
    private TaskExecutorMonitor monitor = TaskExecutorLogMonitor.LOG_MONITOR;
    private JobData jobData = JobData.EMPTY;
    private Task task;
    private T trigger;

    public @NotNull B setVertx(@NotNull Vertx vertx) {
        this.vertx = Objects.requireNonNull(vertx, "Vertx instance is required");
        return (B) this;
    }

    public @NotNull B setMonitor(@NotNull TaskExecutorMonitor monitor) {
        this.monitor = Objects.requireNonNull(monitor, "TaskExecutorMonitor is required");
        return (B) this;
    }

    public @NotNull B setJobData(@NotNull JobData jobData) {
        this.jobData = Objects.requireNonNull(jobData, "JobData is required");
        return (B) this;
    }

    public @NotNull B setTask(@NotNull Task task) {
        this.task = Objects.requireNonNull(task, "Task is required");
        return (B) this;
    }

    public @NotNull B setTrigger(@NotNull T trigger) {
        this.trigger = Objects.requireNonNull(trigger, "Trigger is required");
        return (B) this;
    }

    @Override
    public @NotNull Vertx vertx() { return vertx; }

    @Override
    public @NotNull TaskExecutorMonitor monitor() { return monitor; }

    @Override
    public @NotNull T trigger() { return trigger; }

    @Override
    public @NotNull Task task() { return task; }

    @Override
    public @NotNull JobData jobData() { return jobData; }

}
