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
         * Identify the trigger is scheduled but not yet ready to execute the job
         */
        UNREADY,
        /**
         * Identify the trigger is satisfied every predicate then ready to execute the job
         */
        READY,
        /**
         * Identify the trigger is already run
         */
        EXECUTED,
        /**
         * Identify the trigger is skipped to execute the job
         */
        SKIPPED,
        /**
         * Identify the trigger is stopped by configuration or manually.
         */
        STOPPED,
    }


    class ReasonCode {

        public static final String ON_SCHEDULE = "TriggerIsScheduled";
        public static final String ON_RESCHEDULE = "TriggerIsRescheduled";
        public static final String ON_CANCEL = "TriggerIsCancelled";
        public static final String FAILED_TO_SCHEDULE = "TriggerIsFailedToSchedule";
        public static final String NOT_YET_SCHEDULED = "TriggerIsNotYetScheduled";
        public static final String ALREADY_STOPPED = "TriggerIsAlreadyStopped";
        public static final String CONDITION_IS_NOT_MATCHED = "ConditionIsNotMatched";
        public static final String EVALUATION_TIMEOUT = "TriggerEvaluationTimeout";
        public static final String STOP_BY_JOB = "ForceStop";
        public static final String STOP_BY_CONFIG = "StopByTriggerConfig";
        public static final String JOB_IS_RUNNING = "JobIsRunning";
        public static final String UNEXPECTED_ERROR = "UnexpectedError";

        private ReasonCode() { }

    }

}
