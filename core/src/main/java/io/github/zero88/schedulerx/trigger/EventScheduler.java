package io.github.zero88.schedulerx.trigger;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.Scheduler;
import io.github.zero88.schedulerx.trigger.EventSchedulerImpl.EventSchedulerBuilderImpl;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;

/**
 * An event-base scheduler that triggers to run a job when receive an event from specific {@code event-bus} address.
 *
 * @param <T> Type of event message
 * @see EventTrigger
 * @see EventBus
 * @since 2.0.0
 */
@VertxGen
public interface EventScheduler<T> extends Scheduler<EventTrigger<T>> {

    static <IN, OUT, V> EventSchedulerBuilder<IN, OUT, V> builder() {
        return new EventSchedulerBuilderImpl<>();
    }

    @Override
    @NotNull Vertx vertx();

    @Override
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull EventTrigger<T> trigger();

    @Override
    default void start() { start(null); }

    @Override
    void start(WorkerExecutor workerExecutor);

    @Override
    void cancel();

}
