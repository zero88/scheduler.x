package io.github.zero88.schedulerx.impl;

import java.time.Instant;

import io.github.zero88.schedulerx.TaskResult;

public final class TaskResultBuilder {

    Instant unscheduledAt;
    Instant rescheduledAt;
    Instant availableAt;
    Instant triggeredAt;
    Instant executedAt;
    Instant finishedAt;
    Instant completedAt;
    long tick;
    long round;
    boolean completed;
    Throwable error;
    Object data;

    public TaskResultBuilder setUnscheduledAt(Instant unscheduledAt) {
        this.unscheduledAt = unscheduledAt;
        return this;
    }

    public TaskResultBuilder setRescheduledAt(Instant rescheduledAt) {
        this.rescheduledAt = rescheduledAt;
        return this;
    }

    public TaskResultBuilder setAvailableAt(Instant availableAt) {
        this.availableAt = availableAt;
        return this;
    }

    public TaskResultBuilder setTriggeredAt(Instant triggeredAt) {
        this.triggeredAt = triggeredAt;
        return this;
    }

    public TaskResultBuilder setExecutedAt(Instant executedAt) {
        this.executedAt = executedAt;
        return this;
    }

    public TaskResultBuilder setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
        return this;
    }

    public TaskResultBuilder setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
        return this;
    }

    public TaskResultBuilder setTick(long tick) {
        this.tick = tick;
        return this;
    }

    public TaskResultBuilder setRound(long round) {
        this.round = round;
        return this;
    }

    public TaskResultBuilder setCompleted(boolean completed) {
        this.completed = completed;
        return this;
    }

    public TaskResultBuilder setError(Throwable error) {
        this.error = error;
        return this;
    }

    public TaskResultBuilder setData(Object data) {
        this.data = data;
        return this;
    }

    public TaskResult build() { return new TaskResultImpl(this); }

}
