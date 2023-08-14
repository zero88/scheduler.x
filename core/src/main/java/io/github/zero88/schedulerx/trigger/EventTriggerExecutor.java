package io.github.zero88.schedulerx.trigger;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.TriggerTaskExecutor;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;

@VertxGen
public interface EventTriggerExecutor<INPUT, OUTPUT, T> extends TriggerTaskExecutor<INPUT, OUTPUT, EventTrigger<T>> {

    static <IN, OUT, V> EventTriggerExecutorBuilder<IN, OUT, V> builder() {
        return new EventTriggerExecutorImpl.EventTriggerExecutorBuilderImpl<>();
    }

    @Override
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @NotNull EventTrigger<T> trigger();

}
