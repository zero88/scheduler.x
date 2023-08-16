package io.github.zero88.schedulerx.impl;

import java.time.Instant;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.ExecutionContext;
import io.vertx.core.Promise;

interface ExecutionContextInternal<OUTPUT> extends ExecutionContext<OUTPUT> {

    /**
     * Setup task execution context
     *
     * @param promise    promise
     * @param executedAt execution at time
     * @return a reference to this for fluent API
     * @apiNote It will be invoked by system. In any attempts invoking, {@link IllegalStateException} will be
     *     thrown
     * @see Promise
     */
    @NotNull ExecutionContextInternal<OUTPUT> setup(@NotNull Promise<Object> promise, @NotNull Instant executedAt);

    void internalComplete();

    @Nullable OUTPUT data();

    @Nullable Throwable error();

}
