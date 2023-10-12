package io.github.zero88.schedulerx.trigger;

import java.time.Instant;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.SchedulingMonitor;
import io.github.zero88.schedulerx.Task;
import io.github.zero88.schedulerx.impl.AbstractScheduler;
import io.github.zero88.schedulerx.impl.AbstractSchedulerBuilder;
import io.github.zero88.schedulerx.impl.TriggerContextFactory;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;

final class CronSchedulerImpl<IN, OUT> extends AbstractScheduler<IN, OUT, CronTrigger>
    implements CronScheduler<IN, OUT> {

    private long nextTimerId;

    CronSchedulerImpl(@NotNull Vertx vertx, @NotNull SchedulingMonitor<OUT> monitor, @NotNull JobData<IN> jobData,
                      @NotNull Task<IN, OUT> task, @NotNull CronTrigger trigger) {
        super(vertx, monitor, jobData, task, trigger);
    }

    @Override
    protected @NotNull Future<Long> registerTimer(@NotNull Promise<Long> promise, WorkerExecutor workerExecutor) {
        try {
            final long nextTriggerAfter = trigger().nextTriggerAfter(Instant.now());
            nextTimerId = vertx().setTimer(nextTriggerAfter, timerId -> {
                promise.complete(timerId);
                run(workerExecutor, TriggerContextFactory.kickoff(trigger().type()));
                doStart(workerExecutor);
            });
            log(Instant.now(), "Next schedule after " + nextTriggerAfter + "ms with timerId[" + nextTimerId + "]");
        } catch (Exception ex) {
            promise.fail(ex);
        }
        return promise.future();
    }

    @Override
    protected void unregisterTimer(long timerId) {
        boolean result = vertx().cancelTimer(nextTimerId);
        log(Instant.now(), "Unregistered timerId[" + nextTimerId + "][" + result + "]");
    }

    static final class CronSchedulerBuilderImpl<IN, OUT>
        extends AbstractSchedulerBuilder<IN, OUT, CronTrigger, CronScheduler<IN, OUT>, CronSchedulerBuilder<IN, OUT>>
        implements CronSchedulerBuilder<IN, OUT> {

        public @NotNull CronScheduler<IN, OUT> build() {
            return new CronSchedulerImpl<>(vertx(), monitor(), jobData(), task(), trigger());
        }

    }

}
