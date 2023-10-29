package io.github.zero88.schedulerx.trigger.rule;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a rule that sets advanced conditions to allow the trigger to run or not.
 *
 * @since 2.0.0
 */
@SuppressWarnings("rawtypes")
public interface TriggerRule {

    /**
     * A maximum of leeway time
     */
    Duration MAX_LEEWAY = Duration.ofSeconds(30);

    /**
     * A no-op trigger rule.
     */
    TriggerRule NOOP = create(Collections.emptyList());

    /**
     * Declares the timeframe that allows emitting a trigger
     *
     * @return list of timeframe
     * @see Timeframe
     */
    @JsonGetter
    @NotNull List<Timeframe> timeframes();

    /**
     * Declares the time that the trigger can run until to.
     *
     * @return the until time
     */
    @JsonGetter
    Instant until();

    /**
     * Declares the allowable margin of time in the time validation of {@link #satisfy(Instant)} and {@link #until()}
     *
     * @return the leeway time
     */
    @JsonGetter
    @NotNull Duration leeway();

    /**
     * Check if the fired time is satisfied to at least one timeframe.
     *
     * @param firedAt a clock time that the system timer fires the trigger
     * @return {@code true} if the trigger time is satisfied, otherwise is {@code false}
     * @see #timeframes()
     */
    default boolean satisfy(@NotNull Instant firedAt) {
        return timeframes().isEmpty() ||
               timeframes().stream().anyMatch(timeframe -> timeframe.check(firedAt, leeway()));
    }

    /**
     * Check if the fired time is exceeded the registered {@link #until()} time.
     *
     * @param firedAt a clock time that the system timer fires the trigger
     * @return {@code true} if the trigger time is exceeded, otherwise is {@code false}
     */
    default boolean isExceeded(@NotNull Instant firedAt) {
        return until() != null && firedAt.isAfter(until().plus(leeway()));
    }

    /**
     * Create a new trigger rule
     *
     * @param timeframes the given timeframes
     * @return a new Trigger rule
     */
    static @NotNull TriggerRule create(List<Timeframe> timeframes) {
        return create(timeframes, null, null);
    }


    /**
     * Create a new trigger rule
     *
     * @param timeframes the given timeframes
     * @param leeway the given leeway
     * @return a new Trigger rule
     */
    static @NotNull TriggerRule create(List<Timeframe> timeframes, Duration leeway) {
        return create(timeframes, null, leeway);
    }

    /**
     * Create a new trigger rule
     *
     * @param until the given until
     * @return a new Trigger rule
     */
    static @NotNull TriggerRule create(Instant until) {
        return create(null, until, null);
    }

    /**
     * Create a new trigger rule
     *
     * @param until the given until
     * @param leeway the given leeway
     * @return a new Trigger rule
     */
    static @NotNull TriggerRule create(Instant until, Duration leeway) {
        return create(null, until, leeway);
    }

    /**
     * Create a new trigger rule
     *
     * @param timeframes the given timeframes
     * @param until      the given until
     * @return a new trigger rule
     */
    static @NotNull TriggerRule create(List<Timeframe> timeframes, Instant until) {
        return create(timeframes, until, null);
    }

    /**
     * Create a new trigger rule
     *
     * @param timeframes the given timeframes
     * @param until      the given until
     * @param leeway     the given leeway
     * @return a new trigger rule
     */
    @JsonCreator
    static @NotNull TriggerRule create(@JsonProperty("timeframes") List<Timeframe> timeframes,
                                       @JsonProperty("until") Instant until, @JsonProperty("leeway") Duration leeway) {
        return new TriggerRuleImpl(timeframes, until, leeway);
    }

}
