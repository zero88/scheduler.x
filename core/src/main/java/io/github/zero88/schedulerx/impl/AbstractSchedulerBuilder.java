package io.github.zero88.schedulerx.impl;

import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.Task;
import io.github.zero88.schedulerx.SchedulingLogMonitor;
import io.github.zero88.schedulerx.SchedulingMonitor;
import io.github.zero88.schedulerx.Scheduler;
import io.github.zero88.schedulerx.SchedulerBuilder;
import io.github.zero88.schedulerx.trigger.Trigger;
import io.vertx.core.Vertx;

/**
 * The base scheduler builder
 */
@SuppressWarnings("unchecked")
@Internal
public abstract class AbstractSchedulerBuilder<IN, OUT, T extends Trigger, S extends Scheduler<IN, OUT, T>,
                                                  B extends SchedulerBuilder<IN, OUT, T, S, B>>
    implements SchedulerBuilder<IN, OUT, T, S, B> {

    private Vertx vertx;
    private SchedulingMonitor<OUT> monitor;
    private JobData<IN> jobData;
    private Task<IN, OUT> task;
    private T trigger;

    @Override
    public @NotNull Vertx vertx() {
        return Objects.requireNonNull(vertx, "Vertx instance is required");
    }

    @Override
    public @NotNull SchedulingMonitor<OUT> monitor() {
        return Optional.ofNullable(monitor).orElseGet(SchedulingLogMonitor::create);
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

    public @NotNull B setMonitor(@NotNull SchedulingMonitor<OUT> monitor) {
        this.monitor = monitor;
        return (B) this;
    }

    public @NotNull B setJobData(@NotNull JobData<IN> jobData) {
        this.jobData = jobData;
        return (B) this;
    }

}
