package io.github.zero88.schedulerx.trigger;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.SchedulingMonitor;
import io.github.zero88.schedulerx.Task;
import io.github.zero88.schedulerx.impl.AbstractScheduler;
import io.github.zero88.schedulerx.impl.AbstractSchedulerBuilder;
import io.github.zero88.schedulerx.impl.TriggerContextFactory;
import io.github.zero88.schedulerx.trigger.TriggerCondition.ReasonCode;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;

final class EventSchedulerImpl<IN, OUT, T> extends AbstractScheduler<IN, OUT, EventTrigger<T>>
    implements EventScheduler<IN, OUT, T> {

    private MessageConsumer<Object> consumer;

    EventSchedulerImpl(@NotNull Vertx vertx, @NotNull SchedulingMonitor<OUT> monitor, @NotNull JobData<IN> jobData,
                       @NotNull Task<IN, OUT> task, @NotNull EventTrigger<T> trigger) {
        super(vertx, monitor, jobData, task, trigger);
    }

    @Override
    protected @NotNull Future<Long> registerTimer(@NotNull Promise<Long> promise, WorkerExecutor workerExecutor) {
        final String address = trigger().getAddress();
        consumer = trigger().isLocalOnly()
                   ? vertx().eventBus().localConsumer(address)
                   : vertx().eventBus().consumer(address);
        consumer.handler(msg -> run(workerExecutor, createTriggerContext(msg))).completionHandler(event -> {
            if (event.failed()) {
                promise.fail(new IllegalStateException("Unable to register a subscriber on address[" + address + "]",
                                                       event.cause()));
            } else {
                promise.complete((long) consumer.hashCode());
            }
        });
        return promise.future();
    }

    @Override
    protected void unregisterTimer(long timerId) {
        if (Objects.nonNull(consumer)) {
            consumer.unregister();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected TriggerContext evaluateTrigger(@NotNull TriggerContext triggerContext) {
        final TriggerContext ctx = super.evaluateTrigger(triggerContext);
        try {
            if (ctx.condition().status() == TriggerCondition.TriggerStatus.READY &&
                !trigger().getPredicate().test((T) triggerContext.info())) {
                return TriggerContextFactory.skip(ctx, ReasonCode.CONDITION_IS_NOT_MATCHED);
            }
        } catch (Exception ex) {
            return handleException(ctx, ex);
        }
        return ctx;
    }

    private TriggerContext createTriggerContext(Message<Object> msg) {
        try {
            T eventMsg = trigger().getPredicate().convert(msg.headers(), msg.body());
            return TriggerContextFactory.init(trigger().type(), eventMsg);
        } catch (Exception ex) {
            return handleException(TriggerContextFactory.init(trigger().type(), msg), ex);
        }
    }

    private TriggerContext handleException(TriggerContext context, Exception cause) {
        String reason = cause instanceof ClassCastException
                        ? ReasonCode.CONDITION_IS_NOT_MATCHED
                        : ReasonCode.UNEXPECTED_ERROR;
        return TriggerContextFactory.skip(context, reason, cause);
    }

    // @formatter:off
    static final class EventSchedulerBuilderImpl<IN, OUT, T>
        extends AbstractSchedulerBuilder<IN, OUT, EventTrigger<T>, EventScheduler<IN, OUT, T>, EventSchedulerBuilder<IN, OUT, T>>
        implements EventSchedulerBuilder<IN, OUT, T> {
    // @formatter:on

        public @NotNull EventScheduler<IN, OUT, T> build() {
            return new EventSchedulerImpl<>(vertx(), monitor(), jobData(), task(), trigger());
        }

    }

}
