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
    @Nullable String reasonCode();

    /**
     * The cause that makes the trigger has specific status.
     */
    @Nullable Throwable cause();

    enum TriggerStatus {

        /**
         * Identify the trigger is scheduled successfully
         */
        SCHEDULED,
        /**
         * Identify the trigger is error due to invalid configuration or any unexpected error in system timer
         */
        ERROR,
        /**
         * Identify the trigger is fired by system timer
         */
        KICKOFF,
        /**
         * Identify the trigger is satisfied every predicate then ready to execute the task
         */
        READY,
        /**
         * Identify the trigger is skipped to execute the task
         */
        SKIPPED,
        /**
         * Identify the trigger is stopped by configuration or manually.
         */
        STOPPED,
    }


    class ReasonCode {

        public static final String ON_SCHEDULE = "TriggerIsScheduled";
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
