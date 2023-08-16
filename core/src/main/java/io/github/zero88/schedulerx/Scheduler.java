package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.trigger.Trigger;
import io.vertx.codegen.annotations.GenIgnore;

/**
 * A scheduler schedules a task to run based on a particular trigger.
 *
 * @param <IN>      Type of Input Job data
 * @param <OUT>     Type of task result data
 * @param <TRIGGER> Type of Trigger
 * @apiNote This interface is renamed from {@code TriggerTaskExecutor} since {@code 2.0.0}
 * @see TaskExecutor
 * @see Trigger
 * @since 2.0.0
 */
public interface Scheduler<IN, OUT, TRIGGER extends Trigger> extends TaskExecutor<IN, OUT> {

    /**
     * Trigger type
     *
     * @return trigger
     */
    @NotNull TRIGGER trigger();

    /**
     * Execute task
     *
     * @param executionContext execution context
     */
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    void executeTask(ExecutionContext<OUT> executionContext);

}
