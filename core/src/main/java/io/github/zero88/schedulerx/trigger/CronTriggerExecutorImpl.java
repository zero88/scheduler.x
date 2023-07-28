package io.github.zero88.schedulerx.trigger;

import java.time.Instant;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.Task;
import io.github.zero88.schedulerx.TaskExecutorMonitor;
import io.github.zero88.schedulerx.impl.AbstractTaskExecutor;
import io.github.zero88.schedulerx.impl.AbstractTaskExecutorBuilder;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;

final class CronTriggerExecutorImpl<I> extends AbstractTaskExecutor<I, CronTrigger> implements CronTriggerExecutor<I> {

    CronTriggerExecutorImpl(@NotNull Vertx vertx, @NotNull TaskExecutorMonitor monitor, @NotNull JobData<I> jobData,
                            @NotNull Task<I> task, @NotNull CronTrigger trigger) {
        super(vertx, monitor, jobData, task, trigger);
    }

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

    static final class CronTriggerExecutorBuilderImpl<D>
        extends AbstractTaskExecutorBuilder<D, CronTrigger, CronTriggerExecutor<D>, CronTriggerExecutorBuilder<D>>
        implements CronTriggerExecutorBuilder<D> {

        public @NotNull CronTriggerExecutor<D> build() {
            return new CronTriggerExecutorImpl<>(vertx(), monitor(), jobData(), task(), trigger());
        }

    }

}
