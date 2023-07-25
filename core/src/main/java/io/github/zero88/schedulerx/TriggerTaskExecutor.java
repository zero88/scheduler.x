package io.github.zero88.schedulerx;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.trigger.Trigger;

/**
 * Represents for an executor run task based on particular trigger
 *
 * @param <T> Type of Trigger
 * @see Trigger
 * @since 1.0.0
 */
public interface TriggerTaskExecutor<T extends Trigger> extends TaskExecutor {

    /**
     * Trigger type
     *
     * @return trigger
     */
    @NotNull T trigger();

    /**
     * Task to execute per round
     *
     * @return task
     * @see Task
     */
    @NotNull Task task();

    /**
     * Defines job data as input task data
     *
     * @return job data
     * @see JobData
     */
    @NotNull JobData jobData();

}
