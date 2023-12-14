package io.github.zero88.schedulerx.trigger;

import static io.github.zero88.schedulerx.impl.Utils.brackets;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.Job;
import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.SchedulingMonitor;
import io.github.zero88.schedulerx.TimeoutPolicy;
import io.github.zero88.schedulerx.impl.AbstractScheduler;
import io.github.zero88.schedulerx.impl.AbstractSchedulerBuilder;
import io.github.zero88.schedulerx.impl.TriggerContextFactory;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;

final class CronSchedulerImpl<IN, OUT> extends AbstractScheduler<IN, OUT, CronTrigger>
    implements CronScheduler<IN, OUT> {

    private long nextTimerId;

    CronSchedulerImpl(@NotNull Vertx vertx, @NotNull SchedulingMonitor<OUT> monitor, @NotNull JobData<IN> jobData,
                      @NotNull Job<IN, OUT> job, @NotNull CronTrigger trigger, @NotNull TimeoutPolicy timeoutPolicy) {
        super(vertx, monitor, jobData, job, trigger, timeoutPolicy);
    }

    @Override
    protected @NotNull Future<Long> registerTimer(WorkerExecutor workerExecutor) {
        try {
            final Instant now = Instant.now();
            final long nextTriggerAfter = trigger().nextTriggerAfter(now);
            final Instant nextTriggerTime = now.plus(nextTriggerAfter, ChronoUnit.MILLIS);
            nextTimerId = vertx().setTimer(nextTriggerAfter, tId -> {
                onProcess(workerExecutor, TriggerContextFactory.kickoff(trigger().type(), onFire(tId)));
                doStart(workerExecutor);
            });
            log(now, "Next schedule at" + brackets(nextTriggerTime) + " by timerId" + brackets(nextTimerId));
            return Future.succeededFuture(nextTimerId);
        } catch (Exception ex) {
            return Future.failedFuture(ex);
        }
    }

    @Override
    protected void unregisterTimer(long timerId) {
        boolean result = vertx().cancelTimer(nextTimerId);
        log(Instant.now(), "Unregistered timerId" + brackets(nextTimerId) + brackets(result));
    }

    static final class CronSchedulerBuilderImpl<IN, OUT>
        extends AbstractSchedulerBuilder<IN, OUT, CronTrigger, CronScheduler<IN, OUT>, CronSchedulerBuilder<IN, OUT>>
        implements CronSchedulerBuilder<IN, OUT> {

        public @NotNull CronScheduler<IN, OUT> build() {
            return new CronSchedulerImpl<>(vertx(), monitor(), jobData(), job(), trigger(), timeoutPolicy());
        }

    }

}
