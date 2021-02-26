package io.github.zero88.vertx.scheduler.impl;

import io.github.zero88.vertx.scheduler.TaskExecutionContext;

public interface TaskExecutionContextInternal extends TaskExecutionContext {

    void internalComplete();

    Object data();

    Throwable error();

}
