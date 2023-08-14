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
 * @since 1.0.0
 */
public interface Trigger extends HasTriggerType {

    /**
     * Do validate trigger in runtime
     *
     * @return this for fluent API
     * @throws IllegalArgumentException if any configuration is wrong
     * @since 2.0.0
     */
    @NotNull Trigger validate();

    /**
     * Verify if the trigger time still appropriate to execute the task.
     * <p/>
     * This method will be invoked right away before each execution round is started.
     *
     * @param triggerAt the trigger time
     * @since 2.0.0
     */
    default boolean shouldExecute(@NotNull Instant triggerAt) { return true; }

    /**
     * Verify the execution should be stopped after the current execution round is out of the trigger rule.
     * <p/>
     * This method will be invoked right away after each execution round is finished regardless of the execution result
     * is success or error.
     *
     * @param round the current execution round
     * @since 2.0.0
     */
    default boolean shouldStop(long round) { return false; }

    /**
     * Calculate the next trigger times based on default preview parameter({@link PreviewParameter#byDefault()})
     *
     * @return the list of the next trigger time
     * @since 2.0.0
     */
    default @NotNull List<OffsetDateTime> preview() { return preview(PreviewParameter.byDefault()); }

    /**
     * Calculate the next trigger times based on given preview parameter
     *
     * @param parameter the preview parameter
     * @return the list of the next trigger time
     * @see PreviewParameter
     * @since 2.0.0
     */
    @NotNull List<OffsetDateTime> preview(@NotNull PreviewParameter parameter);

}
