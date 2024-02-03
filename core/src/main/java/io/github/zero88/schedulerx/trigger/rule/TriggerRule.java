package io.github.zero88.schedulerx.trigger.rule;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import io.github.zero88.schedulerx.DefaultOptions;

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
     * A no-op trigger rule.
     */
    TriggerRule NOOP = builder().build();

    /**
     * Declares the timeframe that allows emitting a trigger
     *
     * @return list of timeframe
     * @see Timeframe
     */
    @JsonGetter
    @NotNull List<Timeframe> timeframes();

    /**
     * Declares the future time that the trigger is scheduled.
     * <ul>
     * <li>when value is {@code null} or in the past, the trigger is scheduled immediately</li>
     * <li>when value is in the future, the trigger is pending to the time to schedule</li>
     * </ul>
     *
     * @return the until time
     */
    @JsonGetter
    Instant beginTime();

    /**
     * Declares the time that the trigger can run until to.
     *
     * @return the until time
     */
    @JsonGetter
    Instant until();

    /**
     * Declares the allowable margin of time in the time validation of {@link #satisfy(Instant)} and {@link #until()}.
     * <p>
     * The leeway time has constraints:
     * <ul>
     * <li>when value is negative, the leeway time fallback to {@link Duration#ZERO}</li>
     * <li>when value is greater than {@link DefaultOptions#triggerRuleMaxLeeway}, the leeway time fallback to max
     * default value</li>
     * </ul>
     *
     * @return the leeway time
     */
    @JsonGetter
    @NotNull Duration leeway();

    /**
     * Check whether the fired-at time is before the registered beginning time({@link #beginTime()}).
     * If the beginning time has not yet elapsed, the trigger status is still in pending mode.
     *
     * @param firedAt a clock time that the system timer fires the trigger
     * @return {@code true} if the trigger is not yet ready to run, otherwise is {@code false}
     */
    default boolean isPending(@NotNull Instant firedAt) {
        return beginTime() != null && firedAt.isBefore(beginTime());
    }

    /**
     * Check whether the fired-at time is satisfied to at least one timeframe.
     *
     * @param firedAt a clock time that the system timer fires the trigger
     * @return {@code true} if the fired-at time is satisfied, otherwise is {@code false}
     * @see #timeframes()
     */
    default boolean satisfy(@NotNull Instant firedAt) {
        return timeframes().isEmpty() ||
               timeframes().stream().anyMatch(timeframe -> timeframe.check(firedAt, leeway()));
    }

    /**
     * Check whether the fired-at time is exceeded the registered {@link #until()} time.
     *
     * @param firedAt a clock time that the system timer fires the trigger
     * @return {@code true} if the fired-at time is exceeded, otherwise is {@code false}
     */
    default boolean isExceeded(@NotNull Instant firedAt) {
        return until() != null && firedAt.isAfter(until().plus(leeway()));
    }

    /**
     * Create a builder
     *
     * @return the trigger rule builder
     */
    static TriggerRuleBuilder builder() { return new TriggerRuleBuilder(); }

    /**
     * Create a new trigger rule
     *
     * @param beginTime  the given begin time
     * @param until      the given until
     * @param timeframes the given timeframes
     * @param leeway     the given leeway
     * @return a new trigger rule
     */
    @JsonCreator
    static @NotNull TriggerRule create(@JsonProperty("beginTime") Instant beginTime,
                                       @JsonProperty("until") Instant until,
                                       @JsonProperty("timeframes") List<Timeframe> timeframes,
                                       @JsonProperty("leeway") Duration leeway) {
        return builder().beginTime(beginTime).until(until).timeframes(timeframes).leeway(leeway).build();
    }

}
