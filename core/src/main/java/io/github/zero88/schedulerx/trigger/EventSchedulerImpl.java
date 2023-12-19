package io.github.zero88.schedulerx.trigger;

import static io.github.zero88.schedulerx.impl.Utils.brackets;

import java.time.Instant;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.Job;
import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.SchedulingMonitor;
import io.github.zero88.schedulerx.TimeoutPolicy;
import io.github.zero88.schedulerx.impl.AbstractScheduler;
import io.github.zero88.schedulerx.impl.AbstractSchedulerBuilder;
import io.github.zero88.schedulerx.impl.TriggerContextFactory;
import io.github.zero88.schedulerx.trigger.TriggerCondition.ReasonCode;
import io.github.zero88.schedulerx.trigger.predicate.EventTriggerPredicate.EventTriggerPredicateException;
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
                       @NotNull Job<IN, OUT> job, @NotNull EventTrigger<T> trigger,
                       @NotNull TimeoutPolicy timeoutPolicy) {
        super(vertx, monitor, jobData, job, trigger, timeoutPolicy);
    }

    @Override
    protected @NotNull Future<Long> registerTimer(WorkerExecutor workerExecutor) {
        final Promise<Long> promise = Promise.promise();
        final long timerId = trigger().hashCode();
        final String address = trigger().getAddress();
        consumer = trigger().isLocalOnly()
                   ? vertx().eventBus().localConsumer(address)
                   : vertx().eventBus().consumer(address);
        consumer.handler(msg -> onProcess(workerExecutor, createKickoffContext(msg, onFire(timerId))))
                .completionHandler(event -> {
                    if (event.failed()) {
                        promise.fail(
                            new IllegalStateException("Unable to register a subscriber on address" + brackets(address),
                                                      event.cause()));
                    } else {
                        promise.complete(timerId);
                    }
                });
        return promise.future();
    }

    @Override
    protected void unregisterTimer(long timerId) {
        if (Objects.nonNull(consumer)) {
            consumer.unregister()
                    .onComplete(r -> log(Instant.now(),
                                         "Unregistered EventBus subscriber on address" + brackets(consumer.address()) +
                                         brackets(r.succeeded()) + brackets(r.cause())));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected TriggerContext evaluateTriggerRule(@NotNull TriggerContext triggerContext) {
        final TriggerContext ctx = super.evaluateTriggerRule(triggerContext);
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

    private TriggerContext createKickoffContext(Message<Object> msg, long tick) {
        try {
            T eventMsg = trigger().getPredicate().convert(msg.headers(), msg.body());
            return TriggerContextFactory.kickoff(trigger().type(), tick, eventMsg);
        } catch (Exception ex) {
            return handleException(TriggerContextFactory.kickoff(trigger().type(), tick, msg), ex);
        }
    }

    private TriggerContext handleException(TriggerContext context, Exception cause) {
        String reason = cause instanceof ClassCastException || cause instanceof EventTriggerPredicateException
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
            return new EventSchedulerImpl<>(vertx(), monitor(), jobData(), job(), trigger(), timeoutPolicy());
        }

    }

}
