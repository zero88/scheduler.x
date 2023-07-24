package io.github.zero88.schedulerx.impl;

import java.time.Instant;
import java.util.Objects;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public final class TaskExecutionContextImpl implements TaskExecutionContextInternal {

    private final Vertx vertx;
    private final long round;
    private final Instant triggeredAt;
    private Instant executedAt;
    private Promise<Object> promise;
    private Object data;
    private Throwable error;
    @Accessors(chain = true)
    private boolean forceStop = false;

    @Override
    public @NonNull TaskExecutionContextInternal setup(@NonNull Promise<Object> promise, @NonNull Instant executedAt) {
        if (Objects.nonNull(this.promise)) {
            throw new IllegalStateException("TaskExecutionContext is already setup");
        }
        this.promise = promise;
        this.executedAt = executedAt;
        return this;
    }

    @Override
    public void forceStopExecution() {
        this.forceStop = true;
    }

    @Override
    public void complete(Object data) {
        this.data = data;
        promise.tryComplete(this);
    }

    @Override
    public void fail(Throwable throwable) {
        this.error = throwable;
        promise.tryComplete(this);
    }

    @Override
    public void internalComplete() {
        promise.tryComplete(this);
    }

}
