package io.github.zero88.schedulerx.impl;

import java.time.Instant;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.Task;
import io.github.zero88.schedulerx.TaskExecutorMonitor;
import io.github.zero88.schedulerx.trigger.CronTrigger;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;

public final class CronTaskExecutor extends AbstractTaskExecutor<CronTrigger> {

    private CronTaskExecutor(@NotNull Vertx vertx, @NotNull TaskExecutorMonitor monitor, @NotNull JobData jobData,
                             @NotNull Task task, @NotNull CronTrigger trigger) {
        super(vertx, monitor, jobData, task, trigger);
    }

    public static CronTaskExecutorBuilder builder() { return new CronTaskExecutorBuilder(); }

    @Override
    protected @NotNull Future<Long> addTimer(@NotNull Promise<Long> promise, WorkerExecutor workerExecutor) {
        try {
            final long nextTriggerAfter = trigger().nextTriggerAfter(Instant.now());
            promise.complete(vertx().setTimer(nextTriggerAfter, timerId -> {
                run(workerExecutor);
                start(workerExecutor);
            }));
        } catch (Exception ex) {
            promise.fail(ex);
        }
        return promise.future();
    }

    @Override
    protected boolean shouldCancel(long round) {
        return false;
    }

    public static class CronTaskExecutorBuilder
        extends AbstractTaskExecutorBuilder<CronTrigger, CronTaskExecutor, CronTaskExecutorBuilder> {

        public @NotNull CronTaskExecutor build() {
            return new CronTaskExecutor(vertx(), monitor(), jobData(), task(), trigger());
        }

    }

}
