package io.github.zero88.schedulerx;

import java.time.Instant;

/**
 * Represents for current Task executor state
 *
 * @since 1.0.0
 */
public interface TaskExecutorState {

    /**
     * Timer id
     *
     * @return timer id
     */
    long timerId();

    /**
     * Identifies an executor is on scheduler at time
     *
     * @return available at time
     */
    Instant availableAt();

    /**
     * Current number of times that trigger is fired
     *
     * @return tick
     */
    long tick();

    /**
     * Current number of times that trigger is executed
     *
     * @return round
     */
    long round();

    /**
     * Check whether executor is in {@code pending} state that means is not in a {@code scheduler}
     *
     * @return {@code true} if pending
     */
    boolean pending();

    /**
     * Check whether {@code executor} is in executing state
     *
     * @return {@code true} if in executing
     */
    boolean executing();

    /**
     * Check whether {@code executor} is {@code idle} state that means is in {@code scheduler} but in {@code break-time}
     * between 2 executions
     *
     * @return {@code true} if idle
     */
    default boolean idle() {
        return !executing() && !completed() && !pending();
    }

    /**
     * Check whether {@code executor} is {@code completed} state that means safe to remove out of a {@code scheduler}
     *
     * @return {@code true} if completed
     */
    boolean completed();

    /**
     * Latest data of previous round
     *
     * @return latest data
     */
    Object lastData();

    /**
     * Latest error of previous round
     *
     * @return latest error
     */
    Throwable lastError();

}
