package io.github.zero88.schedulerx.impl;

import io.github.zero88.schedulerx.TaskExecutionContext;

public interface TaskExecutionContextInternal extends TaskExecutionContext {

    void internalComplete();

    Object data();

    Throwable error();

}
