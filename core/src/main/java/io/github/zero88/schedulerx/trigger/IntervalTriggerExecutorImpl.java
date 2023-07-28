package io.github.zero88.schedulerx.trigger;

import java.time.Instant;
import java.util.function.LongSupplier;

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

final class IntervalTriggerExecutorImpl<IN, OUT> extends AbstractTaskExecutor<IN, OUT, IntervalTrigger>
    implements IntervalTriggerExecutor<IN, OUT> {

    IntervalTriggerExecutorImpl(@NotNull Vertx vertx, @NotNull TaskExecutorMonitor<OUT> monitor,
                                @NotNull JobData<IN> jobData, @NotNull Task<IN, OUT> task,
                                @NotNull IntervalTrigger trigger) {
        super(vertx, monitor, jobData, task, trigger);
    }

    protected @NotNull Future<Long> addTimer(@NotNull Promise<Long> promise, WorkerExecutor workerExecutor) {
        try {
            LongSupplier supplier = () -> vertx().setPeriodic(trigger().intervalInMilliseconds(),
                                                              timerId -> run(workerExecutor));
            if (trigger().noDelay()) {
                promise.complete(supplier.getAsLong());
            } else {
                final long delay = trigger().delayInMilliseconds();
                debug(-1, -1, Instant.now(), "delay [" + delay + "ms] then register task in schedule");
                vertx().setTimer(delay, ignore -> promise.complete(supplier.getAsLong()));
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

    static final class IntervalTriggerExecutorBuilderImpl<IN, OUT>
        extends AbstractTaskExecutorBuilder<IN, OUT, IntervalTrigger, IntervalTriggerExecutor<IN, OUT>,
                                               IntervalTriggerExecutorBuilder<IN, OUT>>
        implements IntervalTriggerExecutorBuilder<IN, OUT> {

        public @NotNull IntervalTriggerExecutor<IN, OUT> build() {
            return new IntervalTriggerExecutorImpl<>(vertx(), monitor(), jobData(), task(), trigger());
        }

    }

}
