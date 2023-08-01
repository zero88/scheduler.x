package io.github.zero88.schedulerx.trigger;

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

    @NotNull
    default List<OffsetDateTime> preview() { return preview(PreviewParameter.byDefault()); }

    @NotNull List<OffsetDateTime> preview(@NotNull PreviewParameter parameter);

}
