package io.github.zero88.schedulerx.impl;

import java.time.Instant;
import java.util.function.LongSupplier;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.Task;
import io.github.zero88.schedulerx.TaskExecutorMonitor;
import io.github.zero88.schedulerx.trigger.IntervalTrigger;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;

public final class IntervalTaskExecutor extends AbstractTaskExecutor<IntervalTrigger> {

    private IntervalTaskExecutor(@NotNull Vertx vertx, @NotNull TaskExecutorMonitor monitor, @NotNull JobData jobData,
                                 @NotNull Task task, @NotNull IntervalTrigger trigger) {
        super(vertx, monitor, jobData, task, trigger);
    }

    public static IntervalTaskExecutorBuilder builder() { return new IntervalTaskExecutorBuilder(); }

    protected @NotNull Future<Long> addTimer(@NotNull Promise<Long> promise, WorkerExecutor workerExecutor) {
        try {
            LongSupplier supplier = () -> vertx().setPeriodic(trigger().intervalInMilliseconds(),
                                                              timerId -> run(workerExecutor));
            if (trigger().noDelay()) {
                promise.complete(supplier.getAsLong());
            } else {
                final long delay = trigger().delayInMilliseconds();
                debug(-1, -1, Instant.now(), "delay [" + delay + "ms] then register task in schedule");
                vertx().setTimer(delay, ignore -> promise.complete(supplier.getAsLong()));
            }
        } catch (Exception e) {
            promise.fail(e);
        }
        return promise.future();
    }

    @Override
    protected boolean shouldCancel(long round) {
        return trigger().noRepeatIndefinitely() && round >= trigger().getRepeat();
    }

    public static class IntervalTaskExecutorBuilder
        extends AbstractTaskExecutorBuilder<IntervalTrigger, IntervalTaskExecutor, IntervalTaskExecutorBuilder> {

        public @NotNull IntervalTaskExecutor build() {
            return new IntervalTaskExecutor(vertx(), monitor(), jobData(), task(), trigger());
        }

    }

}
