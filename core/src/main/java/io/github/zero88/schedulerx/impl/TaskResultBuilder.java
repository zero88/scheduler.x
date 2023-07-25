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

    public TaskResultBuilder unscheduledAt(Instant unscheduledAt) {
        this.unscheduledAt = unscheduledAt;
        return this;
    }

    public TaskResultBuilder rescheduledAt(Instant rescheduledAt) {
        this.rescheduledAt = rescheduledAt;
        return this;
    }

    public TaskResultBuilder availableAt(Instant availableAt) {
        this.availableAt = availableAt;
        return this;
    }

    public TaskResultBuilder triggeredAt(Instant triggeredAt) {
        this.triggeredAt = triggeredAt;
        return this;
    }

    public TaskResultBuilder executedAt(Instant executedAt) {
        this.executedAt = executedAt;
        return this;
    }

    public TaskResultBuilder finishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
        return this;
    }

    public TaskResultBuilder completedAt(Instant completedAt) {
        this.completedAt = completedAt;
        return this;
    }

    public TaskResultBuilder tick(long tick) {
        this.tick = tick;
        return this;
    }

    public TaskResultBuilder round(long round) {
        this.round = round;
        return this;
    }

    public TaskResultBuilder completed(boolean completed) {
        this.completed = completed;
        return this;
    }

    public TaskResultBuilder error(Throwable error) {
        this.error = error;
        return this;
    }

    public TaskResultBuilder data(Object data) {
        this.data = data;
        return this;
    }

    public TaskResult build() { return new TaskResultImpl(this); }

}
