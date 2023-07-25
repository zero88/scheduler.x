package io.github.zero88.schedulerx.impl;

import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.TaskExecutionContext;

public interface TaskExecutionContextInternal extends TaskExecutionContext {

    void internalComplete();

    @Nullable Object data();

    @Nullable Throwable error();

}
