package io.github.zero88.schedulerx.trigger;

import java.time.Instant;
import java.util.function.LongSupplier;

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

final class IntervalSchedulerImpl<IN, OUT> extends AbstractScheduler<IN, OUT, IntervalTrigger>
    implements IntervalScheduler<IN, OUT> {

    IntervalSchedulerImpl(@NotNull Vertx vertx, @NotNull SchedulingMonitor<OUT> monitor, @NotNull JobData<IN> jobData,
                          @NotNull Task<IN, OUT> task, @NotNull IntervalTrigger trigger) {
        super(vertx, monitor, jobData, task, trigger);
    }

    protected @NotNull Future<Long> registerTimer(@NotNull Promise<Long> promise, WorkerExecutor workerExecutor) {
        try {
            LongSupplier supplier = () -> vertx().setPeriodic(trigger().intervalInMilliseconds(),
                                                              timerId -> run(workerExecutor, TriggerContextFactory.init(
                                                                  trigger().type())));
            if (trigger().noDelay()) {
                promise.complete(supplier.getAsLong());
            } else {
                final long delay = trigger().delayInMilliseconds();
                log(Instant.now(), "Delay [" + delay + "ms] then register the task in the scheduler");
                vertx().setTimer(delay, ignore -> promise.complete(supplier.getAsLong()));
            }
        } catch (Exception e) {
            promise.fail(e);
        }
        return promise.future();
    }

    // @formatter:off
    static final class IntervalSchedulerBuilderImpl<IN, OUT>
        extends AbstractSchedulerBuilder<IN, OUT, IntervalTrigger, IntervalScheduler<IN, OUT>, IntervalSchedulerBuilder<IN, OUT>>
        implements IntervalSchedulerBuilder<IN, OUT> {
    // @formatter:on

        public @NotNull IntervalScheduler<IN, OUT> build() {
            return new IntervalSchedulerImpl<>(vertx(), monitor(), jobData(), task(), trigger());
        }

    }

}
