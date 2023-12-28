package io.github.zero88.schedulerx.impl;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.HasVertx;
import io.github.zero88.schedulerx.Job;
import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.JobExecutorConfig;
import io.github.zero88.schedulerx.Scheduler;
import io.github.zero88.schedulerx.SchedulerBuilder;
import io.github.zero88.schedulerx.SchedulerConfig;
import io.github.zero88.schedulerx.SchedulingMonitor;
import io.github.zero88.schedulerx.TimeClock;
import io.github.zero88.schedulerx.TimeoutPolicy;
import io.github.zero88.schedulerx.trigger.Trigger;
import io.github.zero88.schedulerx.trigger.TriggerEvaluator;
import io.vertx.core.Vertx;

/**
 * The base scheduler builder
 */
@SuppressWarnings("unchecked")
@Internal
public abstract class AbstractSchedulerBuilder<IN, OUT, T extends Trigger, S extends Scheduler<T>,
                                                  B extends SchedulerBuilder<IN, OUT, T, S, B>>
    implements SchedulerBuilder<IN, OUT, T, S, B>, JobExecutorConfig<IN, OUT>, SchedulerConfig<T, OUT>, HasVertx {

    private Vertx vertx;
    private JobData<IN> jobData;
    private Job<IN, OUT> job;
    private TimeoutPolicy timeoutPolicy;
    private T trigger;
    private TriggerEvaluator evaluator;
    private SchedulingMonitor<OUT> monitor;

    @Override
    public @NotNull Vertx vertx() { return vertx; }

    @Override
    public @Nullable TimeClock clock() { return null; }

    @Override
    public @NotNull SchedulingMonitor<OUT> monitor() { return monitor; }

    @Override
    public @NotNull T trigger() { return trigger; }

    @Override
    public @NotNull TriggerEvaluator triggerEvaluator() { return evaluator; }

    @Override
    public @NotNull Job<IN, OUT> job() { return job; }

    @Override
    public @NotNull JobData<IN> jobData() { return jobData; }

    @Override
    public @NotNull TimeoutPolicy timeoutPolicy() { return timeoutPolicy; }

    public @NotNull B setVertx(@NotNull Vertx vertx) {
        this.vertx = vertx;
        return (B) this;
    }

    public @NotNull B setJob(@NotNull Job<IN, OUT> job) {
        this.job = job;
        return (B) this;
    }

    public @NotNull B setTrigger(@NotNull T trigger) {
        this.trigger = trigger;
        return (B) this;
    }

    @Override
    public @NotNull B setTriggerEvaluator(@NotNull TriggerEvaluator evaluator) {
        this.evaluator = evaluator;
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

    @Override
    public @NotNull B setTimeoutPolicy(@NotNull TimeoutPolicy timeoutPolicy) {
        this.timeoutPolicy = timeoutPolicy;
        return (B) this;
    }

}
