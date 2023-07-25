package io.github.zero88.schedulerx.trigger;

import io.github.zero88.schedulerx.Task;

/**
 * Represents for inspecting settings specific to a Trigger, which is used to fire a <code>{@link Task}</code> at given
 * moments in time
 *
 * @see CronTrigger
 * @see IntervalTrigger
 * @since 1.0.0
 */
public interface Trigger {

    /**
     * Do validate trigger in runtime
     *
     * @return this for fluent API
     * @throws IllegalArgumentException if any configuration is wrong
     */
    Trigger validate();

}
