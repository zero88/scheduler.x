package io.github.zero88.schedulerx.trigger;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.TriggerTaskExecutor;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;

/**
 * Represents for the task executor has an execution loop based on interval.
 *
 * @see IntervalTrigger
 * @since 2.0.0
 */
@VertxGen
public interface IntervalTriggerExecutor extends TriggerTaskExecutor<IntervalTrigger> {

    static IntervalTriggerExecutorBuilder builder() { return new IntervalTriggerExecutorImpl.IntervalTriggerExecutorBuilderImpl(); }

    @Override
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull IntervalTrigger trigger();

}
