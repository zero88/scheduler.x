package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.trigger.Trigger;
import io.vertx.codegen.annotations.GenIgnore;

/**
 * A scheduler schedules a job to run based on a particular trigger.
 *
 * @param <IN>      Type of Job input data
 * @param <OUT>     Type of Job result data
 * @param <TRIGGER> Type of Trigger
 * @apiNote This interface is renamed from {@code TriggerTaskExecutor} since {@code 2.0.0}
 * @see JobExecutor
 * @see Trigger
 * @since 2.0.0
 */
public interface Scheduler<IN, OUT, TRIGGER extends Trigger> extends JobExecutor<IN, OUT> {

    /**
     * Trigger type
     *
     * @return trigger
     */
    @NotNull TRIGGER trigger();

    /**
     * Execute job
     *
     * @param executionContext execution context
     */
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    void executeJob(ExecutionContext<OUT> executionContext);

}
