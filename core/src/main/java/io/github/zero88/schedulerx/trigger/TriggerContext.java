package io.github.zero88.schedulerx.trigger;

import java.time.Instant;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.Scheduler;

/**
 * A runtime trigger information
 *
 * @since 2.0.0
 */
public interface TriggerContext extends HasTriggerType {

    /**
     * The time that the trigger is fired.
     *
     * @apiNote In most case this value is {@code not null}, only when the trigger is cancel manually via
     *     {@link Scheduler#cancel()}, it will be {@code null}
     */
    @Nullable Instant triggerAt();

    /**
     * The current trigger condition
     *
     * @see TriggerCondition
     */
    @NotNull TriggerCondition condition();

    /**
     * The trigger context info
     * <p/>
     * <ul>
     *     <li>In case of the timing-based trigger, this value is {@code null}</li>
     *     <li>In case of the event-based trigger, this value is an event message</li>
     * </ul>
     */
    default @Nullable Object info() { return null; }

}
