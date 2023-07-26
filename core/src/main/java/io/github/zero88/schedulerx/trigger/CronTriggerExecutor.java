package io.github.zero88.schedulerx.trigger;

import io.github.zero88.schedulerx.TriggerTaskExecutor;

/**
 * Represents for the task executor has an execution loop based on the timer of cron expressions.
 *
 * @see CronTrigger
 * @since 2.0.0
 */
public interface CronTriggerExecutor extends TriggerTaskExecutor<CronTrigger> {

    static CronTriggerExecutorBuilder builder() { return new CronTriggerExecutorBuilder(); }

}
