package io.github.zero88.schedulerx.trigger;

import java.time.Instant;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.JobData;
import io.github.zero88.schedulerx.Task;
import io.github.zero88.schedulerx.TaskExecutorMonitor;
import io.github.zero88.schedulerx.impl.AbstractTaskExecutor;
import io.github.zero88.schedulerx.impl.AbstractTaskExecutorBuilder;
import io.github.zero88.schedulerx.impl.InternalTriggerContext;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;

final class EventTriggerExecutorImpl<IN, OUT, T> extends AbstractTaskExecutor<IN, OUT, EventTrigger<T>>
    implements EventTriggerExecutor<IN, OUT, T> {

    private MessageConsumer<Object> consumer;

    EventTriggerExecutorImpl(@NotNull Vertx vertx, @NotNull TaskExecutorMonitor<OUT> monitor,
                             @NotNull JobData<IN> jobData, @NotNull Task<IN, OUT> task,
                             @NotNull EventTrigger<T> trigger) {
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
            final TriggerContext ctx = TriggerContext.create(internalContext.type(), info);
            if (!predicate.test(info)) {
                trace(triggerAt, "Skip the execution due to the trigger context is not matched");
                onMisfire(triggerAt);
                return InternalTriggerContext.create(false, ctx);
            }
            return InternalTriggerContext.create(true, ctx);
        }
        return internalContext;
    }

    // @formatter:off
    static final class EventTriggerExecutorBuilderImpl<IN, OUT, T>
        extends AbstractTaskExecutorBuilder<IN, OUT, EventTrigger<T>, EventTriggerExecutor<IN, OUT, T>, EventTriggerExecutorBuilder<IN, OUT, T>>
        implements EventTriggerExecutorBuilder<IN, OUT, T> {
    // @formatter:on

        public @NotNull EventTriggerExecutor<IN, OUT, T> build() {
            return new EventTriggerExecutorImpl<>(vertx(), monitor(), jobData(), task(), trigger());
        }

    }

}
