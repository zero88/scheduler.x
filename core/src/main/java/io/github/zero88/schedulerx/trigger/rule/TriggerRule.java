package io.github.zero88.schedulerx.trigger.rule;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The trigger rule
 *
 * @since 2.0.0
 */
@SuppressWarnings("rawtypes")
public interface TriggerRule {

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
    @NotNull List<Timeframe> timeFrames();

    /**
     * Declares the time that the trigger can run until to.
     *
     * @return the until time
     */
    @JsonGetter
    Instant until();

    /**
     * Check if the trigger time is satisfied to at least one timeframe.
     *
     * @param triggerAt the trigger time
     * @return {@code true} if the trigger time is satisfied, otherwise is {@code false}
     * @see #timeFrames()
     */
    default boolean satisfy(@NotNull Instant triggerAt) {
        return timeFrames().isEmpty() || timeFrames().stream().anyMatch(timeFrame -> timeFrame.check(triggerAt));
    }

    /**
     * Check if the trigger time is exceeded the registered {@link #until()} time.
     *
     * @param triggerAt the trigger time
     * @return {@code true} if the trigger time is exceeded, otherwise is {@code false}
     */
    default boolean isExceeded(@NotNull Instant triggerAt) {
        return until() != null && triggerAt.isAfter(until());
    }

    /**
     * Create a new trigger rule
     *
     * @param timeframes the given timeframes
     * @return a new Trigger rule
     */
    static @NotNull TriggerRule create(List<Timeframe> timeframes) {
        return create(timeframes, null);
    }

    /**
     * Create a new trigger rule
     *
     * @param until the given until
     * @return a new Trigger rule
     */
    static @NotNull TriggerRule create(Instant until) {
        return create(null, until);
    }

    /**
     * Create a new trigger rule
     *
     * @param timeframes the given timeframes
     * @param until      the given until
     * @return a new trigger rule
     */
    @JsonCreator
    static @NotNull TriggerRule create(@JsonProperty("timeFrames") List<Timeframe> timeframes,
                                       @JsonProperty("until") Instant until) {
        return new TriggerRuleImpl(timeframes, until);
    }

}
