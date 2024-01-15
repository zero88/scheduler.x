package io.github.zero88.schedulerx.impl;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.ExecutionResult;
import io.github.zero88.schedulerx.SchedulingLogMonitor;
import io.github.zero88.schedulerx.SchedulingMonitor;
import io.github.zero88.schedulerx.WorkerExecutorFactory;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

final class SchedulingMonitorImpl<OUT> extends SchedulingMonitorAbstract<OUT>
    implements SchedulingMonitorInternal<OUT> {

    private final WorkerExecutor executor;
    private final SchedulingMonitor<OUT> monitor;

    public SchedulingMonitorImpl(@NotNull Vertx vertx, @Nullable SchedulingMonitor<OUT> monitor) {
        this.executor = WorkerExecutorFactory.createMonitorWorker(vertx);
        this.monitor  = Optional.ofNullable(monitor).orElseGet(SchedulingLogMonitor::create);
    }

    @Override
    protected Logger logger() {
        return LoggerFactory.getLogger(SchedulingMonitorInternal.class);
    }

    @Override
    public void onUnableSchedule(@NotNull ExecutionResult<OUT> result) {
        executor.executeBlocking(event -> dispatch(monitor.getClass(), monitor::onUnableSchedule, result));
    }

    @Override
    public void onSchedule(@NotNull ExecutionResult<OUT> result) {
        executor.executeBlocking(event -> dispatch(monitor.getClass(), monitor::onSchedule, result));
    }

    @Override
    public void onMisfire(@NotNull ExecutionResult<OUT> result) {
        executor.executeBlocking(event -> dispatch(monitor.getClass(), monitor::onMisfire, result));
    }

    @Override
    public void onEach(@NotNull ExecutionResult<OUT> result) {
        executor.executeBlocking(event -> dispatch(monitor.getClass(), monitor::onEach, result));
    }

    @Override
    public void onCompleted(@NotNull ExecutionResult<OUT> result) {
        executor.executeBlocking(event -> dispatch(monitor.getClass(), monitor::onCompleted, result));
    }

    public SchedulingMonitor<OUT> unwrap() {
        return this.monitor;
    }

}
