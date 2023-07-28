package io.github.zero88.schedulerx.trigger;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.TriggerTaskExecutor;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;

/**
 * Represents for the task executor has an execution loop based on interval.
 *
 * @param <INPUT> Type of job input data
 * @see IntervalTrigger
 * @since 2.0.0
 */
@VertxGen
public interface IntervalTriggerExecutor<INPUT> extends TriggerTaskExecutor<INPUT, IntervalTrigger> {

    static <T> IntervalTriggerExecutorBuilder<T> builder() {
        return new IntervalTriggerExecutorImpl.IntervalTriggerExecutorBuilderImpl<>();
    }

    @Override
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull IntervalTrigger trigger();

}
