package io.github.zero88.schedulerx;

import static io.github.zero88.schedulerx.impl.Utils.brackets;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.impl.AbstractScheduler;
import io.github.zero88.schedulerx.impl.AbstractSchedulerBuilder;
import io.github.zero88.schedulerx.impl.TriggerContextFactory;
import io.github.zero88.schedulerx.trigger.CronTrigger;
import io.github.zero88.schedulerx.trigger.TriggerEvaluator;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;

final class CronSchedulerImpl<IN, OUT> extends AbstractScheduler<IN, OUT, CronTrigger> implements CronScheduler {

    private long nextTimerId;

    CronSchedulerImpl(Vertx vertx, TimeClock clock, SchedulingMonitor<OUT> monitor, Job<IN, OUT> job,
                      JobData<IN> jobData, TimeoutPolicy timeoutPolicy, CronTrigger trigger,
                      TriggerEvaluator evaluator) {
        super(vertx, clock, monitor, job, jobData, timeoutPolicy, trigger, evaluator);
    }

    @Override
    protected @NotNull Future<Long> registerTimer(WorkerExecutor workerExecutor) {
        try {
            final Instant now = clock().now();
            final long nextTriggerAfter = trigger().nextTriggerAfter(now);
            final Instant nextTriggerTime = now.plus(nextTriggerAfter, ChronoUnit.MILLIS);
            nextTimerId = vertx().setTimer(nextTriggerAfter, tId -> {
                onProcess(workerExecutor, TriggerContextFactory.kickoff(trigger().type(), clock().now(), onFire(tId)));
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
        log(clock().now(), "Unregistered timerId" + brackets(nextTimerId) + brackets(result));
    }

    static final class CronSchedulerBuilderImpl<IN, OUT>
        extends AbstractSchedulerBuilder<IN, OUT, CronTrigger, CronScheduler, CronSchedulerBuilder<IN, OUT>>
        implements CronSchedulerBuilder<IN, OUT> {

        public @NotNull CronScheduler build() {
            return new CronSchedulerImpl<>(vertx(), clock(), monitor(), job(), jobData(), timeoutPolicy(), trigger(),
                                           triggerEvaluator());
        }

    }

}
