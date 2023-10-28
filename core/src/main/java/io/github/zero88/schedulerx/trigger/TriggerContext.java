package io.github.zero88.schedulerx.trigger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.schedulerx.trigger.TriggerCondition.TriggerStatus;

/**
 * A runtime trigger information
 *
 * @since 2.0.0
 */
public interface TriggerContext extends HasTriggerType {

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

    /**
     * Check whether the trigger has been scheduled.
     */
    default boolean isScheduled() { return TriggerStatus.SCHEDULED == condition().status(); }

    /**
     * Check whether the trigger has invalid configuration or unable to schedule for some reason.
     */
    default boolean isError() { return TriggerStatus.ERROR == condition().status(); }

    /**
     * Check whether the trigger has invalid configuration.
     */
    default boolean isKickoff() { return TriggerStatus.KICKOFF == condition().status(); }

    /**
     * Check whether the trigger is ready or not.
     */
    default boolean isReady() { return TriggerStatus.READY == condition().status(); }

    /**
     * Check whether the trigger is skipped or not.
     */
    default boolean isSkipped() { return TriggerStatus.SKIPPED == condition().status(); }

    /**
     * Check whether the trigger is stopped or not.
     */
    default boolean isStopped() { return TriggerStatus.STOPPED == condition().status(); }

}
