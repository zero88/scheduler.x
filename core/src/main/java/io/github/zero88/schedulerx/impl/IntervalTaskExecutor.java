package io.github.zero88.schedulerx.impl;

import java.time.Instant;
import java.util.function.Supplier;

import io.github.zero88.schedulerx.trigger.IntervalTrigger;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.WorkerExecutor;

import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public final class IntervalTaskExecutor extends AbstractTaskExecutor<IntervalTrigger> {

    protected @NonNull Future<Long> addTimer(@NonNull Promise<Long> promise, WorkerExecutor workerExecutor) {
        try {
            Supplier<Long> supplier = () -> vertx().setPeriodic(trigger().intervalInMilliseconds(),
                                                                timerId -> run(workerExecutor));
            if (trigger().noDelay()) {
                promise.complete(supplier.get());
            } else {
                final long delay = trigger().delayInMilliseconds();
                debug(-1, -1, Instant.now(), "delay [" + delay + "ms] then register task in schedule");
                vertx().setTimer(delay, ignore -> promise.complete(supplier.get()));
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

}
