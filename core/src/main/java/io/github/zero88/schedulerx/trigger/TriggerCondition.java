package io.github.zero88.schedulerx.trigger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the trigger condition to describe more insights when the trigger is ready, skipped, or stopped.
 *
 * @since 2.0.0
 */
public interface TriggerCondition {

    /**
     * The trigger status
     *
     * @see TriggerStatus
     */
    @NotNull TriggerStatus status();

    /**
     * The reason code that makes the trigger has specific status, should be in {@code camelCase} format.
     */
    default @Nullable String reasonCode() { return null; }

    /**
     * The cause that makes the trigger has specific status.
     */
    default @Nullable Throwable cause() { return null; }

    /**
     * Check whether the trigger is failed or not.
     */
    default boolean isFailed() { return TriggerStatus.FAILED == status(); }

    /**
     * Check whether the trigger is ready or not.
     */
    default boolean isReady() { return TriggerStatus.READY == status(); }

    /**
     * Check whether the trigger is skipped or not.
     */
    default boolean isSkip() { return TriggerStatus.SKIP == status(); }

    /**
     * Check whether the trigger is stopped or not.
     */
    default boolean isStop() { return TriggerStatus.STOP == status(); }

    enum TriggerStatus {

        INITIALIZED, FAILED, READY, STOP, SKIP
    }


    class ReasonCode {

        public static final String FAILED_TO_SCHEDULE = "TriggerIsFailedToSchedule";
        public static final String NOT_YET_SCHEDULED = "TriggerIsNotYetScheduled";
        public static final String ALREADY_STOPPED = "TriggerIsAlreadyStopped";
        public static final String CONDITION_IS_NOT_MATCHED = "ConditionIsNotMatched";
        public static final String STOP_BY_TASK = "ForceStopByTask";
        public static final String STOP_BY_CONFIG = "StopByTriggerConfig";
        public static final String STOP_BY_MANUAL = "StopManually";
        public static final String TASK_IS_RUNNING = "TaskIsRunning";
        public static final String UNEXPECTED_ERROR = "UnexpectedError";

        private ReasonCode() { }

    }

}
