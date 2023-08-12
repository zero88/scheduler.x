package io.github.zero88.schedulerx.impl;

import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.ApiStatus.Internal;
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
@Internal
// @formatter:off
public abstract class AbstractTaskExecutorBuilder<IN, OUT, T extends Trigger,
                                                     E extends TriggerTaskExecutor<IN, OUT, T>,
                                                     B extends TriggerTaskExecutorBuilder<IN, OUT, T, E, B>>
    implements TriggerTaskExecutorBuilder<IN, OUT, T, E, B> {
// @formatter:on

    private Vertx vertx;
    private TaskExecutorMonitor<OUT> monitor;
    private JobData<IN> jobData;
    private Task<IN, OUT> task;
    private T trigger;

    @Override
    public @NotNull Vertx vertx() {
        return Objects.requireNonNull(vertx, "Vertx instance is required");
    }

    @Override
    public @NotNull TaskExecutorMonitor<OUT> monitor() {
        return Optional.ofNullable(monitor).orElseGet(TaskExecutorLogMonitor::create);
    }

    @Override
    public @NotNull T trigger() { return Objects.requireNonNull(trigger, "Trigger is required"); }

    @Override
    public @NotNull Task<IN, OUT> task() { return Objects.requireNonNull(task, "Task is required"); }

    @Override
    public @NotNull JobData<IN> jobData() { return Optional.ofNullable(jobData).orElseGet(JobData::empty); }

    public @NotNull B setVertx(@NotNull Vertx vertx) {
        this.vertx = vertx;
        return (B) this;
    }

    public @NotNull B setTask(@NotNull Task<IN, OUT> task) {
        this.task = task;
        return (B) this;
    }

    public @NotNull B setTrigger(@NotNull T trigger) {
        this.trigger = trigger;
        return (B) this;
    }

    public @NotNull B setMonitor(@NotNull TaskExecutorMonitor<OUT> monitor) {
        this.monitor = monitor;
        return (B) this;
    }

    public @NotNull B setJobData(@NotNull JobData<IN> jobData) {
        this.jobData = jobData;
        return (B) this;
    }

}
