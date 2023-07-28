package io.github.zero88.schedulerx.trigger;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.TriggerTaskExecutor;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;

/**
 * Represents for the task executor has an execution loop based on interval.
 *
 * @param <INPUT>  Type of job input data
 * @param <OUTPUT> Type of Result data
 * @see IntervalTrigger
 * @since 2.0.0
 */
@VertxGen
public interface IntervalTriggerExecutor<INPUT, OUTPUT> extends TriggerTaskExecutor<INPUT, OUTPUT, IntervalTrigger> {

    static <IN, OUT> IntervalTriggerExecutorBuilder<IN, OUT> builder() {
        return new IntervalTriggerExecutorImpl.IntervalTriggerExecutorBuilderImpl<>();
    }

    @Override
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull IntervalTrigger trigger();

}
