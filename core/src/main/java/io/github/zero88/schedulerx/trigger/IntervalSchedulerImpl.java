package io.github.zero88.schedulerx.trigger;

import static io.github.zero88.schedulerx.impl.Utils.brackets;

import java.time.Instant;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.SchedulingMonitor;
import io.github.zero88.schedulerx.Task;
import io.github.zero88.schedulerx.TimeoutPolicy;
import io.github.zero88.schedulerx.impl.AbstractScheduler;
import io.github.zero88.schedulerx.impl.AbstractSchedulerBuilder;
import io.github.zero88.schedulerx.impl.TriggerContextFactory;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;

final class IntervalSchedulerImpl<IN, OUT> extends AbstractScheduler<IN, OUT, IntervalTrigger>
    implements IntervalScheduler<IN, OUT> {

    IntervalSchedulerImpl(@NotNull Vertx vertx, @NotNull SchedulingMonitor<OUT> monitor, @NotNull JobData<IN> jobData,
                          @NotNull Task<IN, OUT> task, @NotNull IntervalTrigger trigger,
                          @NotNull TimeoutPolicy timeoutPolicy) {
        super(vertx, monitor, jobData, task, trigger, timeoutPolicy);
    }

    protected @NotNull Future<Long> registerTimer(WorkerExecutor workerExecutor) {
        try {
            if (trigger().noDelay()) {
                return Future.succeededFuture(createPeriodicTimer(workerExecutor));
            }
            final Promise<Long> promise = Promise.promise();
            final long delay = trigger().delayInMilliseconds();
            log(Instant.now(), "Delay " + brackets(delay + "ms") + " then register the trigger in the scheduler");
            vertx().setTimer(delay, ignore -> promise.complete(createPeriodicTimer(workerExecutor)));
            return promise.future();
        } catch (Exception e) {
            return Future.failedFuture(e);
        }
    }

    @Override
    protected void unregisterTimer(long timerId) {
        boolean result = vertx().cancelTimer(timerId);
        log(Instant.now(), "Unregistered timerId" + brackets(timerId) + brackets(result));
    }

    private long createPeriodicTimer(WorkerExecutor executor) {
        return vertx().setPeriodic(trigger().intervalInMilliseconds(), id -> onProcess(executor,
                                                                                       TriggerContextFactory.kickoff(
                                                                                           trigger().type(),
                                                                                           onFire(id))));
    }

    // @formatter:off
    static final class IntervalSchedulerBuilderImpl<IN, OUT>
        extends AbstractSchedulerBuilder<IN, OUT, IntervalTrigger, IntervalScheduler<IN, OUT>, IntervalSchedulerBuilder<IN, OUT>>
        implements IntervalSchedulerBuilder<IN, OUT> {
    // @formatter:on

        public @NotNull IntervalScheduler<IN, OUT> build() {
            return new IntervalSchedulerImpl<>(vertx(), monitor(), jobData(), task(), trigger(), timeoutPolicy());
        }

    }

}
