package io.github.zero88.schedulerx.impl;

import java.time.Instant;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.trigger.TriggerContext;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

final class ExecutionContextImpl<OUTPUT> implements ExecutionContextInternal<OUTPUT> {

    private final Vertx vertx;
    private final long round;
    private final TriggerContext triggerContext;
    private final Instant triggeredAt;
    private Instant executedAt;
    private Promise<Object> promise;
    private OUTPUT data;
    private Throwable error;
    private boolean forceStop = false;

    ExecutionContextImpl(Vertx vertx, long round, TriggerContext triggerContext) {
        this.vertx          = vertx;
        this.round          = round;
        this.triggerContext = triggerContext;
        this.triggeredAt    = Instant.now();
    }

    @Override
    public @NotNull ExecutionContextInternal<OUTPUT> setup(@NotNull Promise<Object> promise,
                                                           @NotNull Instant executedAt) {
        if (Objects.nonNull(this.promise)) {
            throw new IllegalStateException("TaskExecutionContext is already setup");
        }
        this.promise    = promise;
        this.executedAt = executedAt;
        return this;
    }

    @Override
    public @NotNull Vertx vertx() { return vertx; }

    @Override
    public @NotNull TriggerContext triggerContext() { return triggerContext; }

    @Override
    public @NotNull Instant triggeredAt() { return triggeredAt; }

    @Override
    public @NotNull Instant executedAt() { return executedAt; }

    @Override
    public long round() { return round; }

    @Override
    public OUTPUT data() { return data; }

    @Override
    public Throwable error() { return error; }

    @Override
    public boolean isForceStop() { return forceStop; }

    @Override
    public void forceStopExecution() { forceStop = true; }

    @Override
    public void complete(OUTPUT data) {
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
