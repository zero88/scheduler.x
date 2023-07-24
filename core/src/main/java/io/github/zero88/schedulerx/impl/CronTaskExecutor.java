package io.github.zero88.schedulerx.impl;

import java.time.Instant;

import io.github.zero88.schedulerx.trigger.CronTrigger;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.WorkerExecutor;

import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class CronTaskExecutor extends AbstractTaskExecutor<CronTrigger> {

    @Override
    protected @NonNull Future<Long> addTimer(@NonNull Promise<Long> promise, WorkerExecutor workerExecutor) {
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

}
