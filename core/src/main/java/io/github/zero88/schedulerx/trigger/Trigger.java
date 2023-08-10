package io.github.zero88.schedulerx.trigger;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

import org.jetbrains.annotations.NotNull;

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
    @NotNull Trigger validate();

    /**
     * Verify if the trigger time still appropriate to execute the task.
     * <p/>
     * This method will be invoked right away before each execution round is started.
     *
     * @param triggerAt the trigger time
     */
    default boolean shouldExecute(@NotNull Instant triggerAt) { return true; }

    /**
     * Verify the execution should be stopped after the current execution round is out of the trigger rule.
     * <p/>
     * This method will be invoked right away after each execution round is finished regardless of the execution result
     * is success or error.
     *
     * @param round the current execution round
     */
    default boolean shouldStop(long round) { return false; }

    @NotNull
    default List<OffsetDateTime> preview() { return preview(PreviewParameter.byDefault()); }

    @NotNull List<OffsetDateTime> preview(@NotNull PreviewParameter parameter);

}
