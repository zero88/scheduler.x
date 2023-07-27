package io.github.zero88.schedulerx.impl;

import java.time.Instant;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;

final class TaskExecutionContextImpl implements TaskExecutionContextInternal {

    private final Vertx vertx;
    private final long round;
    private final Instant triggeredAt;
    private Instant executedAt;
    private Promise<Object> promise;
    private Object data;
    private Throwable error;
    private boolean forceStop = false;

    public TaskExecutionContextImpl(Vertx vertx, long round, Instant triggeredAt) {
        this.vertx       = vertx;
        this.round       = round;
        this.triggeredAt = triggeredAt;
    }

    @Override
    public @NotNull TaskExecutionContextInternal setup(@NotNull Promise<Object> promise, @NotNull Instant executedAt) {
        if (Objects.nonNull(this.promise)) {
            throw new IllegalStateException("TaskExecutionContext is already setup");
        }
        this.promise    = promise;
        this.executedAt = executedAt;
        return this;
    }

    public @NotNull Vertx vertx()         { return this.vertx; }

    public @NotNull Instant triggeredAt() { return this.triggeredAt; }

    public @NotNull Instant executedAt()  { return this.executedAt; }

    public long round()                   { return this.round; }

    public Object data()                  { return this.data; }

    public Throwable error()              { return this.error; }

    public boolean isForceStop()          { return this.forceStop; }

    @Override
    public void forceStopExecution() {
        this.forceStop = true;
    }

    @Override
    public void complete(Object data) {
        this.data = data;
        this.internalComplete();
    }

    @Override
    public void fail(Throwable throwable) {
        this.error = throwable;
        this.internalComplete();
    }

    @Override
    public void internalComplete() {
        promise.tryComplete(this);
    }

}
