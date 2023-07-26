package io.github.zero88.schedulerx.trigger;

import io.github.zero88.schedulerx.TriggerTaskExecutor;

/**
 * Represents for the task executor has an execution loop based on interval.
 *
 * @see IntervalTrigger
 * @since 2.0.0
 */
public interface IntervalTriggerExecutor extends TriggerTaskExecutor<IntervalTrigger> {

    static IntervalTriggerExecutorBuilder builder() { return new IntervalTriggerExecutorBuilder(); }

}
