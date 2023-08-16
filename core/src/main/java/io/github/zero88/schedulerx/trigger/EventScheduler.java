package io.github.zero88.schedulerx.trigger;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.Scheduler;
import io.github.zero88.schedulerx.trigger.EventSchedulerImpl.EventSchedulerBuilderImpl;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.eventbus.EventBus;

/**
 * An event-base scheduler that triggers to run a task when receive an event from specific {@code event-bus} address.
 *
 * @param <IN>  Type of job input data
 * @param <OUT> Type of task result data
 * @param <T>   Type of event message
 * @see EventTrigger
 * @see EventBus
 * @since 2.0.0
 */
@VertxGen
public interface EventScheduler<IN, OUT, T> extends Scheduler<IN, OUT, EventTrigger<T>> {

    static <IN, OUT, V> EventSchedulerBuilder<IN, OUT, V> builder() {
        return new EventSchedulerBuilderImpl<>();
    }

    @Override
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull EventTrigger<T> trigger();

}
