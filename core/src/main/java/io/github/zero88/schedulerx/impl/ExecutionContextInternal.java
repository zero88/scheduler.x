package io.github.zero88.schedulerx.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.ExecutionContext;
import io.vertx.core.Promise;

interface ExecutionContextInternal<OUTPUT> extends ExecutionContext<OUTPUT> {

    @NotNull TriggerTransitionContext triggerContext();

    /**
     * Prepare to execute task
     *
     * @param promise promise
     * @return a reference to this for fluent API
     * @apiNote It will be invoked by system. In any attempts invoking, {@link IllegalStateException} will be thrown
     */
    @NotNull ExecutionContextInternal<OUTPUT> setup(@NotNull Promise<Object> promise);

    void internalComplete();

    @Nullable OUTPUT data();

    @Nullable Throwable error();

}
