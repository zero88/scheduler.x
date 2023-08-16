package io.github.zero88.schedulerx.trigger;

import java.time.Instant;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.Task;
import io.github.zero88.schedulerx.SchedulingMonitor;
import io.github.zero88.schedulerx.impl.AbstractScheduler;
import io.github.zero88.schedulerx.impl.AbstractSchedulerBuilder;
import io.github.zero88.schedulerx.impl.InternalTriggerContext;
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
        consumer.handler(msg -> run(workerExecutor, TriggerContext.create(trigger().type(), msg)))
                .completionHandler(event -> {
                    if (event.failed()) {
                        promise.fail(
                            new IllegalStateException("Unable to register a subscriber on address[" + address + "]",
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
    protected InternalTriggerContext shouldRun(@NotNull Instant triggerAt, @NotNull TriggerContext triggerContext) {
        final InternalTriggerContext internalContext = super.shouldRun(triggerAt, triggerContext);
        if (internalContext.shouldRun()) {
            final EventTriggerPredicate<T> predicate = trigger().getPredicate();
            final Message<Object> msg = (Message<Object>) internalContext.info();
            final T info = predicate.convert(msg.headers(), msg.body());
            final boolean shouldRun = predicate.test(info);
            if (!shouldRun) {
                onMisfire(triggerAt, "The event trigger info is not matched");
            }
            final TriggerContext ctx = TriggerContext.create(internalContext.type(), info);
            return InternalTriggerContext.create(shouldRun, ctx);
        }
        return internalContext;
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
