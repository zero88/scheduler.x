package io.github.zero88.schedulerx.impl;

import java.time.Instant;

import io.github.zero88.schedulerx.TaskResult;

final class TaskResultImpl<OUTPUT> implements TaskResult<OUTPUT> {

    private final Instant unscheduledAt;
    private final Instant rescheduledAt;
    private final Instant availableAt;
    private final Instant triggeredAt;
    private final Instant executedAt;
    private final Instant finishedAt;
    private final Instant completedAt;
    private final long tick;
    private final long round;
    private final boolean completed;
    private final Throwable error;
    private final OUTPUT data;

    TaskResultImpl(TaskResultBuilder<OUTPUT> builder) {
        this.unscheduledAt = builder.unscheduledAt;
        this.rescheduledAt = builder.rescheduledAt;
        this.availableAt   = builder.availableAt;
        this.triggeredAt   = builder.triggeredAt;
        this.executedAt    = builder.executedAt;
        this.finishedAt    = builder.finishedAt;
        this.completedAt   = builder.completedAt;
        this.tick          = builder.tick;
        this.round         = builder.round;
        this.completed     = builder.completed;
        this.error         = builder.error;
        this.data          = builder.data;
    }

    public Instant unscheduledAt() { return this.unscheduledAt; }

    public Instant rescheduledAt() { return this.rescheduledAt; }

    public Instant availableAt()   { return this.availableAt; }

    public Instant triggeredAt()   { return this.triggeredAt; }

    public Instant executedAt()    { return this.executedAt; }

    public Instant finishedAt()    { return this.finishedAt; }

    public Instant completedAt()   { return this.completedAt; }

    public long tick()             { return this.tick; }

    public long round()            { return this.round; }

    public boolean isCompleted()   { return this.completed; }

    public Throwable error()       { return this.error; }

    public OUTPUT data()           { return this.data; }

    /**
     * Create builder
     *
     * @return TaskResultBuilder
     */
    static <OUT> TaskResultBuilder<OUT> builder() { return new TaskResultBuilder<>(); }

    /**
     * Represents a builder that constructs {@link TaskResult}
     *
     * @see TaskResult
     */
    static final class TaskResultBuilder<OUTPUT> {

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
        OUTPUT data;

        public TaskResultBuilder<OUTPUT> setUnscheduledAt(Instant unscheduledAt) {
            this.unscheduledAt = unscheduledAt;
            return this;
        }

        public TaskResultBuilder<OUTPUT> setRescheduledAt(Instant rescheduledAt) {
            this.rescheduledAt = rescheduledAt;
            return this;
        }

        public TaskResultBuilder<OUTPUT> setAvailableAt(Instant availableAt) {
            this.availableAt = availableAt;
            return this;
        }

        public TaskResultBuilder<OUTPUT> setTriggeredAt(Instant triggeredAt) {
            this.triggeredAt = triggeredAt;
            return this;
        }

        public TaskResultBuilder<OUTPUT> setExecutedAt(Instant executedAt) {
            this.executedAt = executedAt;
            return this;
        }

        public TaskResultBuilder<OUTPUT> setFinishedAt(Instant finishedAt) {
            this.finishedAt = finishedAt;
            return this;
        }

        public TaskResultBuilder<OUTPUT> setCompletedAt(Instant completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public TaskResultBuilder<OUTPUT> setTick(long tick) {
            this.tick = tick;
            return this;
        }

        public TaskResultBuilder<OUTPUT> setRound(long round) {
            this.round = round;
            return this;
        }

        public TaskResultBuilder<OUTPUT> setCompleted(boolean completed) {
            this.completed = completed;
            return this;
        }

        public TaskResultBuilder<OUTPUT> setError(Throwable error) {
            this.error = error;
            return this;
        }

        public TaskResultBuilder<OUTPUT> setData(OUTPUT data) {
            this.data = data;
            return this;
        }

        public TaskResult<OUTPUT> build() { return new TaskResultImpl<>(this); }

    }

}
