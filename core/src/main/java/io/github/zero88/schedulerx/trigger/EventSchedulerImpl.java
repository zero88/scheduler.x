package io.github.zero88.schedulerx.trigger;

import static io.github.zero88.schedulerx.impl.Utils.brackets;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.Job;
import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.SchedulingMonitor;
import io.github.zero88.schedulerx.TimeClock;
import io.github.zero88.schedulerx.TimeoutPolicy;
import io.github.zero88.schedulerx.impl.AbstractScheduler;
import io.github.zero88.schedulerx.impl.AbstractSchedulerBuilder;
import io.github.zero88.schedulerx.impl.DefaultTriggerEvaluator;
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
    implements EventScheduler<T> {

    private MessageConsumer<Object> consumer;

    EventSchedulerImpl(Vertx vertx, TimeClock clock, SchedulingMonitor<OUT> monitor, Job<IN, OUT> job,
                       JobData<IN> jobData, TimeoutPolicy timeoutPolicy, EventTrigger<T> trigger,
                       TriggerEvaluator evaluator) {
        super(vertx, clock, monitor, job, jobData, timeoutPolicy, trigger,
              new EventTriggerEvaluator<>().andThen(evaluator));
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
                    .onComplete(r -> log(clock().now(),
                                         "Unregistered EventBus subscriber on address" + brackets(consumer.address()) +
                                         brackets(r.succeeded()) + brackets(r.cause())));
        }
    }

    private TriggerContext createKickoffContext(Message<Object> msg, long tick) {
        try {
            T eventMsg = trigger().getPredicate().convert(msg.headers(), msg.body());
            return TriggerContextFactory.kickoff(trigger().type(), clock().now(), tick, eventMsg);
        } catch (Exception ex) {
            return handleException(TriggerContextFactory.kickoff(trigger().type(), clock().now(), tick, msg), ex);
        }
    }

    static TriggerContext handleException(TriggerContext context, Exception cause) {
        String reason = cause instanceof ClassCastException || cause instanceof EventTriggerPredicateException
                        ? ReasonCode.CONDITION_IS_NOT_MATCHED
                        : ReasonCode.UNEXPECTED_ERROR;
        return TriggerContextFactory.skip(context, reason, cause);
    }

    static final class EventSchedulerBuilderImpl<IN, OUT, T>
        extends AbstractSchedulerBuilder<IN, OUT, EventTrigger<T>, EventScheduler<T>, EventSchedulerBuilder<IN, OUT, T>>
        implements EventSchedulerBuilder<IN, OUT, T> {

        public @NotNull EventScheduler<T> build() {
            return new EventSchedulerImpl<>(vertx(), clock(), monitor(), job(), jobData(), timeoutPolicy(), trigger(),
                                            triggerEvaluator());
        }

    }


    static final class EventTriggerEvaluator<T> extends DefaultTriggerEvaluator {

        @Override
        @SuppressWarnings("unchecked")
        protected Future<TriggerContext> internalBeforeTrigger(@NotNull Trigger trigger, @NotNull TriggerContext ctx,
                                                               @Nullable Object externalId) {
            try {
                if (!((EventTrigger<T>) trigger).getPredicate().test((T) ctx.info())) {
                    return Future.succeededFuture(TriggerContextFactory.skip(ctx, ReasonCode.CONDITION_IS_NOT_MATCHED));
                }
            } catch (Exception ex) {
                return Future.succeededFuture(handleException(ctx, ex));
            }
            return Future.succeededFuture(ctx);
        }

    }

}
