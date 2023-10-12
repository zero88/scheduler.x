package io.github.zero88.schedulerx.impl;

import java.time.Instant;

import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.ExecutionResult;
import io.github.zero88.schedulerx.trigger.TriggerContext;

final class ExecutionResultImpl<OUTPUT> implements ExecutionResult<OUTPUT> {

    static <OUT> ExecutionResultBuilder<OUT> builder() { return new ExecutionResultBuilder<>(); }

    private final Instant unscheduledAt;
    private final Instant rescheduledAt;
    private final Instant availableAt;
    private final Instant firedAt;
    private final Instant triggeredAt;
    private final Instant executedAt;
    private final Instant finishedAt;
    private final Instant completedAt;
    private final Object externalId;
    private final long tick;
    private final long round;
    private final TriggerContext triggerContext;
    private final Throwable error;
    private final OUTPUT data;

    ExecutionResultImpl(ExecutionResultBuilder<OUTPUT> builder) {
        this.unscheduledAt  = builder.unscheduledAt;
        this.rescheduledAt  = builder.rescheduledAt;
        this.availableAt    = builder.availableAt;
        this.firedAt        = builder.firedAt;
        this.triggeredAt    = builder.triggeredAt;
        this.executedAt     = builder.executedAt;
        this.finishedAt     = builder.finishedAt;
        this.completedAt    = builder.completedAt;
        this.externalId     = builder.externalId;
        this.tick           = builder.tick;
        this.round          = builder.round;
        this.triggerContext = builder.triggerContext;
        this.error          = builder.error;
        this.data           = builder.data;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable T externalId() { return (T) this.externalId; }

    @Override
    public TriggerContext triggerContext() { return this.triggerContext; }

    public Instant unscheduledAt() { return this.unscheduledAt; }

    public Instant availableAt()   { return this.availableAt; }

    public Instant rescheduledAt() { return this.rescheduledAt; }

    public Instant firedAt()       { return this.firedAt; }

    public Instant triggeredAt()   { return this.triggeredAt; }

    public Instant executedAt()    { return this.executedAt; }

    public Instant finishedAt()    { return this.finishedAt; }

    public Instant completedAt()   { return this.completedAt; }

    public long tick()             { return this.tick; }

    public long round()            { return this.round; }

    public Throwable error()       { return this.error; }

    public OUTPUT data()           { return this.data; }

    static final class ExecutionResultBuilder<OUTPUT> {

        Instant unscheduledAt;
        Instant availableAt;
        Instant rescheduledAt;
        Instant firedAt;
        Instant triggeredAt;
        Instant executedAt;
        Instant finishedAt;
        Instant completedAt;
        Object externalId;
        long tick;
        long round;
        TriggerContext triggerContext;
        Throwable error;
        OUTPUT data;

        public ExecutionResultBuilder<OUTPUT> setUnscheduledAt(Instant unscheduledAt) {
            this.unscheduledAt = unscheduledAt;
            return this;
        }

        public ExecutionResultBuilder<OUTPUT> setAvailableAt(Instant availableAt) {
            this.availableAt = availableAt;
            return this;
        }

        public ExecutionResultBuilder<OUTPUT> setRescheduledAt(Instant rescheduledAt) {
            this.rescheduledAt = rescheduledAt;
            return this;
        }

        public ExecutionResultBuilder<OUTPUT> setFiredAt(Instant firedAt) {
            this.firedAt = firedAt;
            return this;
        }

        public ExecutionResultBuilder<OUTPUT> setTriggeredAt(Instant triggeredAt) {
            this.triggeredAt = triggeredAt;
            return this;
        }

        public ExecutionResultBuilder<OUTPUT> setExecutedAt(Instant executedAt) {
            this.executedAt = executedAt;
            return this;
        }

        public ExecutionResultBuilder<OUTPUT> setFinishedAt(Instant finishedAt) {
            this.finishedAt = finishedAt;
            return this;
        }

        public ExecutionResultBuilder<OUTPUT> setCompletedAt(Instant completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public ExecutionResultBuilder<OUTPUT> setExternalId(Object externalId) {
            this.externalId = externalId;
            return this;
        }

        public ExecutionResultBuilder<OUTPUT> setTick(long tick) {
            this.tick = tick;
            return this;
        }

        public ExecutionResultBuilder<OUTPUT> setRound(long round) {
            this.round = round;
            return this;
        }

        public ExecutionResultBuilder<OUTPUT> setTriggerContext(TriggerContext triggerContext) {
            this.triggerContext = TriggerContextFactory.convert(triggerContext);
            return this;
        }

        public ExecutionResultBuilder<OUTPUT> setError(Throwable error) {
            this.error = error;
            return this;
        }

        public ExecutionResultBuilder<OUTPUT> setData(OUTPUT data) {
            this.data = data;
            return this;
        }

        public ExecutionResult<OUTPUT> build() { return new ExecutionResultImpl<>(this); }

    }

}
