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

@SuppressWarnings("unchecked")
abstract class AbstractTaskExecutorBuilder<T extends Trigger, E extends TriggerTaskExecutor<T>, B extends AbstractTaskExecutorBuilder<T, E, B>>
    implements TriggerTaskExecutorBuilder<T, TaskExecutionContextInternal, E, B> {

    private Vertx vertx;
    private TaskExecutorMonitor monitor = TaskExecutorLogMonitor.LOG_MONITOR;
    private JobData jobData = JobData.EMPTY;
    private Task task;
    private T trigger;

    public @NotNull B setVertx(@NotNull Vertx vertx) {
        this.vertx = Objects.requireNonNull(vertx);
        return (B) this;
    }

    public @NotNull B setMonitor(@NotNull TaskExecutorMonitor monitor) {
        this.monitor = Objects.requireNonNull(monitor);
        return (B) this;
    }

    public @NotNull B setJobData(@NotNull JobData jobData) {
        this.jobData = jobData;
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

    @NotNull
    @Override
    public Vertx vertx() { return vertx; }

    @NotNull
    @Override
    public TaskExecutorMonitor monitor() { return monitor; }

    @NotNull
    @Override
    public JobData jobData() { return jobData; }

    @NotNull
    @Override
    public Task task() { return task; }

    @NotNull
    @Override
    public T trigger() { return trigger; }

}
