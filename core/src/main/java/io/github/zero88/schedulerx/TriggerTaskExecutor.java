package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.trigger.Trigger;
import io.github.zero88.schedulerx.trigger.TriggerContext;
import io.vertx.codegen.annotations.GenIgnore;

/**
 * Represents for an executor run task based on particular trigger
 *
 * @param <INPUT>   Type of Input Job data
 * @param <OUTPUT>  Type of Result data
 * @param <TRIGGER> Type of Trigger
 * @see TaskExecutor
 * @see Trigger
 * @see TriggerContext
 * @since 1.0.0
 */
public interface TriggerTaskExecutor<INPUT, OUTPUT, TRIGGER extends Trigger> extends TaskExecutor<INPUT, OUTPUT> {

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
    void executeTask(TaskExecutionContext<OUTPUT> executionContext);

}
