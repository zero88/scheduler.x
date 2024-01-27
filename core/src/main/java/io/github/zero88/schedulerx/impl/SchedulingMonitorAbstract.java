package io.github.zero88.schedulerx.impl;

import static io.github.zero88.schedulerx.impl.Utils.brackets;

import java.util.Objects;
import java.util.function.Consumer;

import io.github.zero88.schedulerx.ExecutionResult;
import io.github.zero88.schedulerx.SchedulingMonitor;
import io.vertx.core.impl.logging.Logger;

abstract class SchedulingMonitorAbstract<OUT> implements SchedulingMonitor<OUT> {

    protected abstract Logger logger();

    protected final void dispatch(Class<? extends SchedulingMonitor> monitorCls,
                                  Consumer<ExecutionResult<OUT>> dispatcher, ExecutionResult<OUT> result) {
        try {
            if (Objects.nonNull(dispatcher)) {
                dispatcher.accept(result);
            }
        } catch (Throwable ex) {
            logger().warn(
                "Unexpected error in " + brackets(monitorCls.getName()) + " when dispatching the execution result", ex);
        }
    }

}
