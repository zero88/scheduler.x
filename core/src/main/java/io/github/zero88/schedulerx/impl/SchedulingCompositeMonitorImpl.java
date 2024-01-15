package io.github.zero88.schedulerx.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.ExecutionResult;
import io.github.zero88.schedulerx.SchedulingCompositeMonitor;
import io.github.zero88.schedulerx.SchedulingMonitor;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

public final class SchedulingCompositeMonitorImpl<OUT> extends SchedulingMonitorAbstract<OUT>
    implements SchedulingCompositeMonitor<OUT> {

    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends SchedulingMonitor>, SchedulingMonitor<OUT>> monitors = new HashMap<>();

    @Override
    protected Logger logger() {
        return LoggerFactory.getLogger(SchedulingCompositeMonitor.class);
    }

    @Override
    public SchedulingCompositeMonitor<OUT> register(SchedulingMonitor<OUT> monitor) {
        if (Objects.nonNull(monitor)) {
            this.monitors.put(monitor.getClass(), monitor);
        }
        return this;
    }

    @Override
    public void onUnableSchedule(@NotNull ExecutionResult<OUT> result) {
        monitors.forEach(((aClass, monitor) -> dispatch(aClass, monitor::onUnableSchedule, result)));
    }

    @Override
    public void onSchedule(@NotNull ExecutionResult<OUT> result) {
        monitors.forEach(((aClass, monitor) -> dispatch(aClass, monitor::onSchedule, result)));
    }

    @Override
    public void onMisfire(@NotNull ExecutionResult<OUT> result) {
        monitors.forEach(((aClass, monitor) -> dispatch(aClass, monitor::onMisfire, result)));
    }

    @Override
    public void onEach(@NotNull ExecutionResult<OUT> result) {
        monitors.forEach(((aClass, monitor) -> dispatch(aClass, monitor::onEach, result)));
    }

    @Override
    public void onCompleted(@NotNull ExecutionResult<OUT> result) {
        monitors.forEach(((aClass, monitor) -> dispatch(aClass, monitor::onCompleted, result)));
    }

}
