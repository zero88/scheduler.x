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
                                                     B extends TriggerTaskExecutorBuilder<T, E, B>>
    implements TriggerTaskExecutorBuilder<T, E, B> {

    private Vertx vertx;
    private TaskExecutorMonitor monitor = TaskExecutorLogMonitor.LOG_MONITOR;
    private JobData jobData = JobData.EMPTY;
    private Task task;
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
    public @NotNull Task task() { return Objects.requireNonNull(task, "Task is required"); }

    @Override
    public @NotNull JobData jobData() { return Objects.requireNonNull(jobData, "JobData is required"); }

    public @NotNull B setVertx(@NotNull Vertx vertx) {
        this.vertx = vertx;
        return (B) this;
    }

    public @NotNull B setTask(@NotNull Task task) {
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

    public @NotNull B setJobData(@NotNull JobData jobData) {
        this.jobData = jobData;
        return (B) this;
    }

}
