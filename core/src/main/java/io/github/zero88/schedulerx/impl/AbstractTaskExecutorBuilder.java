package io.github.zero88.schedulerx.impl;

import java.util.Objects;
import java.util.Optional;

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
public abstract class AbstractTaskExecutorBuilder<I, T extends Trigger, E extends TriggerTaskExecutor<I, T>,
                                                     B extends TriggerTaskExecutorBuilder<I, T, E, B>>
    implements TriggerTaskExecutorBuilder<I, T, E, B> {

    private Vertx vertx;
    private TaskExecutorMonitor monitor = TaskExecutorLogMonitor.LOG_MONITOR;
    private JobData<I> jobData;
    private Task<I> task;
    private T trigger;

    @Override
    public @NotNull Vertx vertx() {
        return Objects.requireNonNull(vertx, "Vertx instance is required");
    }

    @Override
    public @NotNull TaskExecutorMonitor monitor() {
        return Objects.requireNonNull(monitor, "TaskExecutorMonitor is required");
    }

    @Override
    public @NotNull T trigger() { return Objects.requireNonNull(trigger, "Trigger is required"); }

    @Override
    public @NotNull Task<I> task() { return Objects.requireNonNull(task, "Task is required"); }

    @Override
    public @NotNull JobData<I> jobData() {
        return Optional.ofNullable(jobData).orElseGet(JobData::empty);
    }

    public @NotNull B setVertx(@NotNull Vertx vertx) {
        this.vertx = vertx;
        return (B) this;
    }

    public @NotNull B setTask(@NotNull Task<I> task) {
        this.task = task;
        return (B) this;
    }

    public @NotNull B setTrigger(@NotNull T trigger) {
        this.trigger = trigger;
        return (B) this;
    }

    public @NotNull B setMonitor(@NotNull TaskExecutorMonitor monitor) {
        this.monitor = monitor;
        return (B) this;
    }

    public @NotNull B setJobData(@NotNull JobData<I> jobData) {
        this.jobData = jobData;
        return (B) this;
    }

}
