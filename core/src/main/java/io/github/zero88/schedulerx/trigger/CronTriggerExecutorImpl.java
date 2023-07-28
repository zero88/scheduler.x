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

final class CronTriggerExecutorImpl<IN, OUT> extends AbstractTaskExecutor<IN, OUT, CronTrigger>
    implements CronTriggerExecutor<IN, OUT> {

    CronTriggerExecutorImpl(@NotNull Vertx vertx, @NotNull TaskExecutorMonitor<OUT> monitor,
                            @NotNull JobData<IN> jobData, @NotNull Task<IN, OUT> task, @NotNull CronTrigger trigger) {
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

    static final class CronTriggerExecutorBuilderImpl<IN, OUT> extends
                                                               AbstractTaskExecutorBuilder<IN, OUT, CronTrigger,
                                                                                              CronTriggerExecutor<IN,
                                                                                                                     OUT>, CronTriggerExecutorBuilder<IN, OUT>>
        implements CronTriggerExecutorBuilder<IN, OUT> {

        public @NotNull CronTriggerExecutor<IN, OUT> build() {
            return new CronTriggerExecutorImpl<>(vertx(), monitor(), jobData(), task(), trigger());
        }

    }

}
