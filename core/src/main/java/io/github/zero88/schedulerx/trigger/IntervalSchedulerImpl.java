package io.github.zero88.schedulerx.trigger;

import static io.github.zero88.schedulerx.impl.Utils.brackets;
import static io.github.zero88.schedulerx.trigger.IntervalTrigger.REPEAT_INDEFINITELY;

import java.time.Duration;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.Job;
import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.SchedulingMonitor;
import io.github.zero88.schedulerx.TimeClock;
import io.github.zero88.schedulerx.TimeoutPolicy;
import io.github.zero88.schedulerx.impl.AbstractScheduler;
import io.github.zero88.schedulerx.impl.AbstractSchedulerBuilder;
import io.github.zero88.schedulerx.impl.TriggerContextFactory;
import io.github.zero88.schedulerx.trigger.TriggerCondition.ReasonCode;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;

final class IntervalSchedulerImpl<IN, OUT> extends AbstractScheduler<IN, OUT, IntervalTrigger>
    implements IntervalScheduler {

    IntervalSchedulerImpl(Vertx vertx, TimeClock clock, SchedulingMonitor<OUT> monitor, Job<IN, OUT> job,
                          JobData<IN> jobData, TimeoutPolicy timeoutPolicy, IntervalTrigger trigger,
                          TriggerEvaluator evaluator) {
        super(vertx, clock, monitor, job, jobData, timeoutPolicy, trigger, createTriggerEvaluator().andThen(evaluator));
    }

    protected @NotNull Future<Long> registerTimer(WorkerExecutor workerExecutor) {
        try {
            if (Duration.ZERO.compareTo(trigger().initialDelay()) == 0) {
                return Future.succeededFuture(createPeriodicTimer(workerExecutor));
            }
            final Promise<Long> promise = Promise.promise();
            final long delay = trigger().initialDelay().toMillis();
            log(clock().now(), "Delay " + brackets(delay + "ms") + " then register the trigger in the scheduler");
            vertx().setTimer(delay, ignore -> promise.complete(createPeriodicTimer(workerExecutor)));
            return promise.future();
        } catch (Exception e) {
            return Future.failedFuture(e);
        }
    }

    @Override
    protected void unregisterTimer(long timerId) {
        boolean result = vertx().cancelTimer(timerId);
        log(clock().now(), "Unregistered timerId" + brackets(timerId) + brackets(result));
    }

    private long createPeriodicTimer(WorkerExecutor executor) {
        final long millis = trigger().interval().toMillis();
        return this.vertx()
                   .setPeriodic(millis, id -> onProcess(executor,
                                                        TriggerContextFactory.kickoff(trigger().type(), clock().now(),
                                                                                      onFire(id))));
    }

    static final class IntervalSchedulerBuilderImpl<IN, OUT>
        extends AbstractSchedulerBuilder<IN, OUT, IntervalTrigger, IntervalScheduler, IntervalSchedulerBuilder<IN, OUT>>
        implements IntervalSchedulerBuilder<IN, OUT> {

        public @NotNull IntervalScheduler build() {
            return new IntervalSchedulerImpl<>(vertx(), clock(), monitor(), job(), jobData(), timeoutPolicy(),
                                               trigger(), triggerEvaluator());
        }

    }

    static TriggerEvaluator createTriggerEvaluator() {
        return TriggerEvaluator.byAfter((trigger, triggerContext, externalId, round) -> {
            IntervalTrigger interval = (IntervalTrigger) trigger;
            if (interval.getRepeat() != REPEAT_INDEFINITELY && round >= interval.getRepeat()) {
                return Future.succeededFuture(TriggerContextFactory.stop(triggerContext, ReasonCode.STOP_BY_CONFIG));
            }
            return Future.succeededFuture(triggerContext);
        });
    }

}
